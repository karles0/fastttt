package FacturaFast.facturaFast.empresa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "empresas")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String razonSocial; //nombre de la empresa

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(nullable = false, length = 6)
    private String ubigeo;

    @Column(name = "usuario_sol", nullable = false)
    private String usuarioSol; //credenciales SOL

    @Column(name = "clave_sol", nullable = false)
    private String claveSol;

    @Column(name = "ruta_certificado", nullable = false)
    private String rutaCertificado; //es el certificado digital, se va a enconotrar en una ruta local

    @Column(name = "clave_certificado", nullable = false)
    private String claveCertificado;

    @Builder.Default
    @Column(name = "is_production")
    private Boolean isProduction = false;



}
