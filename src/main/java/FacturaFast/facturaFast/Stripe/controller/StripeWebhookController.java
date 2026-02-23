package FacturaFast.facturaFast.Stripe.controller;

import FacturaFast.facturaFast.Suscripcion.service.SuscripcionService;
import FacturaFast.facturaFast.Stripe.service.StripeCheckoutService;
import FacturaFast.facturaFast.whatsapp.service.WhatsAppService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final StripeCheckoutService stripeCheckoutService;
    private final SuscripcionService suscripcionService;
    private final WhatsAppService whatsAppService;

    public StripeWebhookController(StripeCheckoutService stripeCheckoutService,
                                   SuscripcionService suscripcionService,
                                   WhatsAppService whatsAppService) {
        this.stripeCheckoutService = stripeCheckoutService;
        this.suscripcionService = suscripcionService;
        this.whatsAppService = whatsAppService;
    }

    @GetMapping("/checkout")
    public ResponseEntity<Void> createCheckoutAndRedirect(@RequestParam Long waId) throws StripeException {
        Session session = stripeCheckoutService.createCheckoutSession(waId);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, session.getUrl())
                .build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String signature) {
        Event event;
        try {
            event = stripeCheckoutService.constructEvent(payload, signature);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> onCheckoutCompleted(event);
            case "customer.subscription.updated", "customer.subscription.deleted" -> onSubscriptionStatusChanged(event);
            default -> {
            }
        }
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> success(@RequestParam("session_id") String sessionId) {
        return ResponseEntity.ok(Map.of("message", "Pago recibido", "sessionId", sessionId));
    }

    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancel() {
        return ResponseEntity.ok(Map.of("message", "Pago cancelado"));
    }

    private void onCheckoutCompleted(Event event) {
        try {
            StripeCheckoutService.ActivationData data = stripeCheckoutService.extractActivationData(event);
            if (data == null) return;

            suscripcionService.activarPorCheckout(
                    data.sessionId(),
                    data.customerId(),
                    data.subscriptionId(),
                    data.periodEnd()
            );

            if (data.waId() != null) {
                whatsAppService.enviarBienvenidaSuscripcionActiva(data.waId());
            }
        } catch (Exception ignored) {
        }
    }

    private void onSubscriptionStatusChanged(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (!(stripeObject instanceof Subscription subscription)) {
            return;
        }

        Instant periodEnd = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
        String status = subscription.getStatus();

        if ("active".equalsIgnoreCase(status)) {
            suscripcionService.actualizarPeriodo(subscription.getId(), periodEnd);
            return;
        }

        if ("canceled".equalsIgnoreCase(status)
                || "unpaid".equalsIgnoreCase(status)
                || "past_due".equalsIgnoreCase(status)
                || "incomplete_expired".equalsIgnoreCase(status)) {
            Long waId = suscripcionService.marcarInactivaPorSubscriptionId(subscription.getId(), status, periodEnd);
            if (waId != null) {
                whatsAppService.enviarSuscripcionVencida(waId);
            }
        }
    }
}
