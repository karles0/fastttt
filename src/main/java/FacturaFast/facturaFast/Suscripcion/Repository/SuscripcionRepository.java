package FacturaFast.facturaFast.Suscripcion.Repository;

import FacturaFast.facturaFast.Suscripcion.entity.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    Optional<Suscripcion> findByUsuarioWaId(Long waId);
    Optional<Suscripcion> findByStripeCheckoutSessionId(String sessionId);
    Optional<Suscripcion> findByStripeSubscriptionId(String subscriptionId);
}
