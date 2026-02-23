package FacturaFast.facturaFast.Stripe.service;

import FacturaFast.facturaFast.Suscripcion.service.SuscripcionService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class StripeCheckoutService {

    private final SuscripcionService suscripcionService;

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @Value("${stripe.webhook-signature-key:}")
    private String webhookSignatureKey;

    @Value("${stripe.price-id:}")
    private String priceId;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public StripeCheckoutService(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }


    public String buildHostedCheckoutEntryPoint(Long waId) {
        return appBaseUrl + "/api/stripe/checkout?waId=" + waId;
    }

    public Session createCheckoutSession(Long waId) throws StripeException {
        Stripe.apiKey = secretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(appBaseUrl + "/api/stripe/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(appBaseUrl + "/api/stripe/cancel")
                .putMetadata("waId", waId.toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .build();

        Session session = Session.create(params);
        suscripcionService.marcarPending(waId, session.getId());
        return session;
    }

    public Event constructEvent(String payload, String signatureHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signatureHeader, webhookSignatureKey);
    }

    public ActivationData extractActivationData(Event event) throws StripeException {
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (!(stripeObject instanceof Session session)) {
            return null;
        }

        Long waId = null;
        Map<String, String> metadata = session.getMetadata();
        if (metadata != null && metadata.get("waId") != null) {
            waId = Long.parseLong(metadata.get("waId"));
        }

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        Instant periodEnd = null;
        if (subscriptionId != null && !subscriptionId.isBlank()) {
            Stripe.apiKey = secretKey;
            Subscription subscription = Subscription.retrieve(subscriptionId);
            if (subscription.getCurrentPeriodEnd() != null) {
                periodEnd = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
            }
        }

        return new ActivationData(waId, session.getId(), customerId, subscriptionId, periodEnd);
    }

    public record ActivationData(Long waId, String sessionId, String customerId, String subscriptionId, Instant periodEnd) {
    }
}
