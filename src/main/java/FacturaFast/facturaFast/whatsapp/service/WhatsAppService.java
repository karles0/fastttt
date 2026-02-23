package FacturaFast.facturaFast.whatsapp.service;

import FacturaFast.facturaFast.Suscripcion.service.SuscripcionService;
import FacturaFast.facturaFast.Stripe.service.StripeCheckoutService;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import FacturaFast.facturaFast.usuario.service.UsuarioBotService;
import FacturaFast.facturaFast.whatsapp.parser.IncomingWa;
import FacturaFast.facturaFast.whatsapp.parser.IncomingWaParser;
import org.springframework.stereotype.Service;
import java.util.Map;

import java.util.Map;

@Service
public class WhatsAppService {

    private final UsuarioBotService usuarioBotService;
    private final SuscripcionService suscripcionService;
    private final StripeCheckoutService stripeCheckoutService;
    private final WhatsAppApiClient apiClient;

    public WhatsAppService(UsuarioBotService usuarioBotService,
                           SuscripcionService suscripcionService,
                           StripeCheckoutService stripeCheckoutService,
                           WhatsAppApiClient apiClient) {
        this.usuarioBotService = usuarioBotService;
        this.suscripcionService = suscripcionService;
        this.stripeCheckoutService = stripeCheckoutService;
        this.apiClient = apiClient;
    }

    public void tryAutoReply(Map<String, Object> payload) {
        IncomingWa wa = IncomingWaParser.parse(payload);
        if (wa == null || wa.waId() == null) return;
        Long waIdLong;
        try {
            waIdLong = Long.parseLong(wa.waId());
        } catch (NumberFormatException ex) {
            return;
        }
        String nombre = wa.profileName();
        String texto = wa.textBody();
        UsuarioBot usuario = usuarioBotService.getOrCreateUsuario(waIdLong, nombre);
        String checkoutLink = stripeCheckoutService.buildHostedCheckoutEntryPoint(waIdLong);

        if (!Boolean.TRUE.equals(usuario.getOnboardingEnviado())) {
            apiClient.sendText(wa.waId(), buildOnboardingMessage(checkoutLink));
            usuarioBotService.marcarOnboardingEnviado(waIdLong);
            return;
        }

        if (!suscripcionService.isActiva(waIdLong)) {
            apiClient.sendText(wa.waId(), buildPaymentReminderMessage(checkoutLink));
            return;
        }

        if (texto != null && texto.equalsIgnoreCase("hola")) {
            apiClient.sendButtons(
                    wa.waId(),
                    "¿Cómo estás?",
                    "Elige una opción",
                    Map.of(
                            "MOOD_BIEN", "Bien",
                            "MOOD_MAL", "Mal"
                    )
            );
            return;
        }

        if (wa.buttonId() != null) {
            switch (wa.buttonId()) {
                case "MOOD_BIEN" -> apiClient.sendText(wa.waId(), "Perfecto. ¿Qué necesitas hoy: emitir una factura o registrar tu empresa?");
                case "MOOD_MAL" -> apiClient.sendText(wa.waId(), "Entiendo. Dime qué pasó y te ayudo.");
                default -> apiClient.sendText(wa.waId(), "Ok.");
            }
            return;
        }

        apiClient.sendText(wa.waId(), "Te leo. Escribe 'hola' para empezar.");
    }

    public void enviarBienvenidaSuscripcionActiva(Long waId) {
        apiClient.sendText(waId.toString(), "✅ ¡Pago confirmado! Tu suscripción de FacturaFast ya está activa.\n\n"
                + "Ya puedes usar el bot para emitir comprobantes conectados a SUNAT.");
    }

    public void enviarSuscripcionVencida(Long waId) {
        String checkoutLink = stripeCheckoutService.buildHostedCheckoutEntryPoint(waId);
        apiClient.sendText(waId.toString(), "⚠️ Tu suscripción de FacturaFast venció.\n\n"
                + "Para volver a usar el servicio, renueva tu suscripción aquí:\n" + checkoutLink);
    }

    private String buildOnboardingMessage(String linkPago) { return "Hola, soy FacturaFast.\n\n"
            + "Te ayudo a emitir facturas electrónicas conectadas a SUNAT: registras los datos por WhatsApp y yo genero, firmo y envío el comprobante.\n\n"
            + "Para usar el servicio necesitas una suscripción activa. Si deseas continuar, realiza el pago aquí:\n"
            + linkPago + "\n\n"
            + "Cuando se confirme tu suscripción, te escribiré automáticamente para empezar.";
    }

    private String buildPaymentReminderMessage(String linkPago) {
        return "Aún no se registró un pago activo para tu cuenta.\n\n"
                + "Para activar FacturaFast realiza tu suscripción aquí:\n"
                + linkPago;
    }
}
