package FacturaFast.facturaFast.usuario.controller;

import FacturaFast.facturaFast.factura.entity.Factura;
import FacturaFast.facturaFast.factura.service.FacturaService;
import FacturaFast.facturaFast.usuario.entity.EstadoBot;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import FacturaFast.facturaFast.usuario.service.TelegramService;
import FacturaFast.facturaFast.usuario.service.UsuarioBotService;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/telegram")
public class TelegramHookController {

    private final UsuarioBotService usuarioBotService;
    private final FacturaService facturaService;
    private final TelegramService telegramSender;

    public TelegramHookController(UsuarioBotService usuarioBotService, FacturaService facturaService,
                                  TelegramService telegramService){

        this.usuarioBotService = usuarioBotService;
        this.facturaService = facturaService;
        this.telegramSender = telegramService;
    }

    @PostMapping("/webhook")
    public void RecibirActualizacion(@RequestBody Update update){

        if(!update.hasMessage() || !update.getMessage().hasText()) return;

        String texto = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();
        String firstName = update.getMessage().getFrom().getFirstName();

        UsuarioBot usuario = usuarioBotService.getOrCreateUsuario(chatId, firstName, username);
        try {
            if(texto.equals("/start")){
                telegramSender.enviarMensaje(chatId, "Hola! "+ firstName + "!\n"
                + "Bienvenido a **FacturaFast SaaS**.\n" + "Usa `/emitir [RUC]` para facturar.\n" +
                "Usa `/registro` para registrar tu empresa.");
                return;
            }
            if(texto.startsWith("/emitir")){
                procesarComandoEmitir(usuario, texto);
                return;
            }

            if(texto.startsWith("/terminar")){
                procesarFinFactura(usuario);
                return;
            }

            switch(usuario.getEstadoActual()){
                case ADDING_ITEMS:
                    procesarNuevoItem(usuario,texto);
                    break;
                case WAITING_RUC:
                    procesarComandoEmitir(usuario, "/emitir " + texto);
                    break;
                case IDLE:
                    telegramSender.enviarMensaje(chatId, "Comando no valido, usa `/emitir [RUC]` para empezar.");
                    break;
            }

        } catch (Exception e){
            e.printStackTrace();
            telegramSender.enviarMensaje(chatId, "**Error**" + e.getMessage());
        }

    }

    private void procesarComandoEmitir(UsuarioBot usuario, String texto){
        // /emitir 1234123
        String[] partes = texto.split(" "); //separo por espacio
        if(partes.length < 2){
            telegramSender.enviarMensaje(usuario.getWaId(), "Falta el ruc");
        }
        String rucCliente = partes[1];

        Factura factura = facturaService.iniciarBorrador(usuario.getWaId(), rucCliente);
        telegramSender.enviarMensaje(usuario.getWaId(),
                " **Factura Borrador Creada**\n" +
                        "Cliente: " + factura.getClienteNombre() + "\n" +
                        "Serie: " + factura.getSerie() + "-" + factura.getCorrelativo() + "\n" +
                        "-----------------------------------\n" +
                        "**Agrega ítems** enviando:\n" +
                        "`Cantidad, Descripción, Precio unitario total`\n" +
                        "Ejemplo: `2, Coca Cola, 5.50`");

    }

    private void procesarFinFactura(UsuarioBot usuario){
        if(usuario.getFacturaActual() == null){
            telegramSender.enviarMensaje(usuario.getWaId(), "No tienes alguna factura activa");
            return;
        }
        telegramSender.enviarMensaje(usuario.getWaId(), "Espere un momento mientras generamos la factura");

        // aqui falta la logica de coneccion por medio de soap
        telegramSender.enviarMensaje(usuario.getWaId(), "**¡Factura Aceptada!**\\n(Aquí iría el PDF adjunto)");
        usuarioBotService.actualizarEstado(usuario.getWaId(), EstadoBot.IDLE);
    }

    private void procesarNuevoItem(UsuarioBot usuario, String texto){
        //vamos a leer como un csv, por comas

        try {
            String[] partes = texto.split(",");
            if(partes.length != 3){
                throw new IllegalArgumentException("Formato incorrecto, usa: Cantidad, Descripcion(producto), Precio");
            }

            BigDecimal cantidad = new BigDecimal(partes[0].trim());
            String descripcion = partes[1].trim();
            BigDecimal precioConIgv = new BigDecimal(partes[2].trim());

            Factura factura = facturaService.agregarDetalle(usuario.getWaId(), cantidad, descripcion, precioConIgv);

            /* PUEDE IR ESO O UN "Item agregado, sigue agregando o envia /terminar para generar la factura "
            telegramSender.enviarMensaje(usuario.getTelegramId(),
                    "**Ítem Agregado**\n" +
                    cantidad + " x " + descripcion + "\n" +
                    "Total Acumulado: S/ " + factura.getTotalCantidad() + "\n" +
                    "-----------------------------------\n" +
                    "Sigue agregando o envía `/terminar` para generar la factura.");

             */


        }catch (NumberFormatException e){
            telegramSender.enviarMensaje(usuario.getTelegramId(), "Error: Usa punto (.) para decimales");
        } catch (Exception e){
            telegramSender.enviarMensaje(usuario.getTelegramId(), e.getMessage());
        }
    }


}
