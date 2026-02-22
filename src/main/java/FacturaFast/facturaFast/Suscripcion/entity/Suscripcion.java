package FacturaFast.facturaFast.Suscripcion.entity;

import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "suscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wa_id", nullable = false, unique = true)
    private UsuarioBot usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EstadoSuscripcion estado;

    @Column(name = "stripe_customer_id", length = 64)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 64)
    private String stripeSubscriptionId;

    @Column(name = "stripe_checkout_session_id", length = 64)
    private String stripeCheckoutSessionId;

    private Instant currentPeriodEnd;

    private Instant updatedAt;
}
