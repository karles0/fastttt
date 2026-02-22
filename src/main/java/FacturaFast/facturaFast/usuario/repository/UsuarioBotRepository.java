package FacturaFast.facturaFast.usuario.repository;


import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioBotRepository extends JpaRepository<UsuarioBot, Long> {

}
