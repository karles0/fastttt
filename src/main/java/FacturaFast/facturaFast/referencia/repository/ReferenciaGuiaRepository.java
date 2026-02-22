package FacturaFast.facturaFast.referencia.repository;

import FacturaFast.facturaFast.referencia.entity.ReferenciaGuia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferenciaGuiaRepository extends JpaRepository<ReferenciaGuia, Long> {

    List<ReferenciaGuia> findByFacturaId(Long facturaId);

}
