package FacturaFast.facturaFast.Suscripcion.service;

import FacturaFast.facturaFast.Suscripcion.Repository.SuscripcionRepository;
import FacturaFast.facturaFast.Suscripcion.entity.EstadoSuscripcion;
import FacturaFast.facturaFast.Suscripcion.entity.Suscripcion;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import FacturaFast.facturaFast.usuario.repository.UsuarioBotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SuscripcionService {

    private final SuscripcionRepository repo;
    private final UsuarioBotRepository usuarioRepo;

    public SuscripcionService(SuscripcionRepository repo, UsuarioBotRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public Suscripcion getOrCreate(Long waId) {
        return repo.findByUsuarioWaId(waId).orElseGet(() -> {
            UsuarioBot u = usuarioRepo.findById(waId).orElseThrow();
            Suscripcion s = Suscripcion.builder()
                    .usuario(u)
                    .estado(EstadoSuscripcion.NONE)
                    .updatedAt(Instant.now())
                    .build();
            return repo.save(s);
        });
    }

    public boolean isActiva(Long waId) {
        return repo.findByUsuarioWaId(waId)
                .map(s -> s.getEstado() == EstadoSuscripcion.ACTIVE
                        && (s.getCurrentPeriodEnd() == null || s.getCurrentPeriodEnd().isAfter(Instant.now())))
                .orElse(false);
    }

    @Transactional
    public void marcarPending(Long waId, String sessionId) {
        Suscripcion s = getOrCreate(waId);
        s.setEstado(EstadoSuscripcion.PENDING);
        s.setStripeCheckoutSessionId(sessionId);
        s.setUpdatedAt(Instant.now());
        repo.save(s);
    }

    @Transactional
    public void activarPorCheckout(String sessionId, String customerId, String subscriptionId, Instant periodEnd) {
        Suscripcion s = repo.findByStripeCheckoutSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Checkout session no encontrada: " + sessionId));

        s.setEstado(EstadoSuscripcion.ACTIVE);
        s.setStripeCustomerId(customerId);
        s.setStripeSubscriptionId(subscriptionId);
        s.setCurrentPeriodEnd(periodEnd);
        s.setUpdatedAt(Instant.now());
        repo.save(s);
    }

    @Transactional
    public void actualizarPeriodo(String subscriptionId, Instant periodEnd) {
        repo.findByStripeSubscriptionId(subscriptionId).ifPresent(s -> {
            s.setEstado(EstadoSuscripcion.ACTIVE);
            s.setCurrentPeriodEnd(periodEnd);
            s.setUpdatedAt(Instant.now());
            repo.save(s);
        });
    }

    @Transactional
    public Long marcarInactivaPorSubscriptionId(String subscriptionId, String stripeStatus, Instant periodEnd) {
        Suscripcion s = repo.findByStripeSubscriptionId(subscriptionId).orElse(null);
        if (s == null) return null;

        EstadoSuscripcion estado = switch (stripeStatus.toLowerCase()) {
            case "past_due", "unpaid" -> EstadoSuscripcion.PAST_DUE;
            default -> EstadoSuscripcion.CANCELED;
        };

        s.setEstado(estado);
        s.setCurrentPeriodEnd(periodEnd);
        s.setUpdatedAt(Instant.now());
        repo.save(s);
        return s.getUsuario().getWaId();
    }
}
