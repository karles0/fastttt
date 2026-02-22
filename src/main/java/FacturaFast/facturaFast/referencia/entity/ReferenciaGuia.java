package FacturaFast.facturaFast.referencia.entity;


import FacturaFast.facturaFast.factura.entity.Factura;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guias_referencia")
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ReferenciaGuia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @Column(length = 4, nullable = false)
    private String series;

    @Column(length = 15, nullable = false)
    private String numero;

    @Builder.Default
    @Column(length = 2)
    private String tipoCodigo = "09"; // guia remitente por defecto

}
