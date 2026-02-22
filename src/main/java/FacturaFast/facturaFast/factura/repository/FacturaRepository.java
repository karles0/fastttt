package FacturaFast.facturaFast.factura.repository;

import FacturaFast.facturaFast.factura.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @Query("SELECT MAX(f.correlativo) FROM Factura f WHERE f.empresa.id =: empresaId AND f.tipo='FACTURA'")
    Integer findMaxCorrelativoByEmpresaId(@Param("empresaId") Long empresaId);
}
