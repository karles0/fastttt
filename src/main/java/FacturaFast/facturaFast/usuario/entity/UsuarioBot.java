package FacturaFast.facturaFast.usuario.entity;


import FacturaFast.facturaFast.empresa.entity.Empresa;
import FacturaFast.facturaFast.factura.entity.Factura;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios_bot")
@Data
@NoArgsConstructor
public class UsuarioBot {

    @Id
    private Long waId; //id de whatsapp

    private String primerNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoBot estadoActual = EstadoBot.IDLE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura_actual")
    private Factura facturaActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa_seleccionada")
    private Empresa empresaSeleccionada;

    @Column(nullable = false)
    private Boolean onboardingEnviado = false;


}
