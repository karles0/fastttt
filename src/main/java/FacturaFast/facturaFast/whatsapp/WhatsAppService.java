package FacturaFast.facturaFast.whatsapp;

import FacturaFast.facturaFast.Suscripcion.service.SuscripcionService;
import FacturaFast.facturaFast.usuario.service.UsuarioBotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private final UsuarioBotService usuarioBotService;
    private final SuscripcionService suscripcionService;
    private final WhatsAppApiClient apiClient; // tu cliente para enviar mensajes

    @Value("${app.checkout_url}")
    private String checkoutUrl;

    public WhatsAppService(UsuarioBotService usuarioBotService,
                           SuscripcionService suscripcionService,
                           WhatsAppApiClient apiClient) {
        this.usuarioBotService = usuarioBotService;
        this.suscripcionService = suscripcionService;
        this.apiClient = apiClient;
    }

    public void tryAutoReply(Map<String, Object> payload) {
        // 1) Extraer datos
        IncomingWa wa = IncomingWaParser.parse(payload);
        if (wa == null || wa.waId() == null) return;

        Long waIdLong = Long.parseLong(wa.waId()); // waId viene como string numérico
        String nombre = wa.profileName();
        String texto = wa.textBody();

        // 2) get/create usuario
        UsuarioBot usuario = usuarioBotService.getOrCreateUsuario(waIdLong, nombre);

        // 3) onboarding 1 sola vez
        if (!Boolean.TRUE.equals(usuario.getOnboardingEnviado())) {
            apiClient.sendText(wa.waId(), buildOnboardingMessage(checkoutUrl));
            usuarioBotService.marcarOnboardingEnviado(waIdLong);
            return;
        }

        // 4) gate por suscripción
        if (!suscripcionService.isActiva(waIdLong)) {
            apiClient.sendText(wa.waId(),
                    "Aún no estás suscrito a FacturaFast. Para activar el servicio realiza el pago aquí:\n" + checkoutUrl);
            return;
        }

        // 5) Test: “cómo estás” + botones
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

        // 6) Respuesta a botones (interactive)
        if (wa.buttonId() != null) {
            switch (wa.buttonId()) {
                case "MOOD_BIEN" -> apiClient.sendText(wa.waId(), "Perfecto. ¿Qué necesitas hoy: emitir una factura o registrar tu empresa?");
                case "MOOD_MAL" -> apiClient.sendText(wa.waId(), "Entiendo. Dime qué pasó y te ayudo.");
                default -> apiClient.sendText(wa.waId(), "Ok.");
            }
            return;
        }

        // fallback
        apiClient.sendText(wa.waId(), "Te leo. Escribe 'hola' para empezar.");
    }

    private String buildOnboardingMessage(String linkPago) {
        return "Hola, soy FacturaFast.\n\n" +
                "Te ayudo a emitir facturas electrónicas conectadas a SUNAT: registras los datos por WhatsApp y yo genero, firmo y envío el comprobante.\n\n" +
                "Para usar el servicio necesitas una suscripción activa. Si deseas continuar, realiza el pago aquí:\n" +
                linkPago + "\n\n" +
                "Cuando se confirme tu suscripción, te escribiré automáticamente para empezar.";
    }
}