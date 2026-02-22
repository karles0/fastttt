package FacturaFast.facturaFast.detalle.repository;

import FacturaFast.facturaFast.detalle.entity.DetallePreciso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePrecisoRepository extends JpaRepository<DetallePreciso, Long> {
    // SELECT * FROM detalles WHERE factura_id = ?
    List<DetallePreciso> findByFactura_Id(Long facturaId);
}
