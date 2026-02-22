package FacturaFast.facturaFast.factura.entity;



import FacturaFast.facturaFast.detalle.entity.DetallePreciso;
import FacturaFast.facturaFast.empresa.entity.Empresa;
import FacturaFast.facturaFast.referencia.entity.ReferenciaGuia;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "facturas")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private UsuarioBot usuario;

    @Column(length = 4, nullable = false)
    private String serie; // ejemplo "F001"

    @Column(nullable = false)
    private Integer correlativo; // 1234

    @Enumerated(EnumType.STRING)
    private Tipo tipo;
    //aqui ya agarramos la informacion del cliente, a quien pertenece el ruc
    @Column(length = 11, nullable = false)
    private String clienteRuc;

    @Column(nullable = false)
    private String clienteNombre;

    @Column(length = 100) //le queria poner nullable false, pero quiza vaya a fallar
    private String direccionCliente;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    @Builder.Default
    @Column(length = 3)
    private String currency = "PEN"; //todas las operaciones por defecto son en soles

    //el precio
    private BigDecimal totalGravada;
    private BigDecimal totalIgv;
    private BigDecimal totalCantidad; //importe total

    @Enumerated(EnumType.STRING)
    private EstadoFactura estadoFactura;


    @Lob
    @Column(columnDefinition = "TEXT")
    private String signedXmlContent; //documento firmado en xml. aqui hacemos el xml firmado con lo anterior puesto

    @Lob
    private byte[] cdrContent; //creamos el CDR y lo exportaremos en formato .zip

    // qr
    private String xmlHash;

    @Column(length = 1000)
    private String sunatErrorLog; //por si hay error al entrar a sunat

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePreciso> items = new ArrayList<>();

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReferenciaGuia> guias = new ArrayList<>();

}
