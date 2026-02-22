package FacturaFast.facturaFast.detalle.entity;


import FacturaFast.facturaFast.factura.entity.Factura;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "detalles_factura")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetallePreciso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    private BigDecimal cantidad;
    private String descripcion;

    private BigDecimal precioUnitarioConIgv; // Lo que escribe el usuario
    private BigDecimal valorUnitario;
    private BigDecimal igv;
    private BigDecimal costoTotal; // precio con igv * cantidad

    @Builder.Default
    @Column(length = 5)
    private String unitCode = "NIU";

    @Builder.Default
    @Column(length = 5)
    private String tipoIgv = "10"; // "10" Gravado - operacion Onerosa

}
