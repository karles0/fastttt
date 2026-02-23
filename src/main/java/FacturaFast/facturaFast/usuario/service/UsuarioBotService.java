package FacturaFast.facturaFast.usuario.service;

import FacturaFast.facturaFast.usuario.entity.EstadoBot;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import FacturaFast.facturaFast.usuario.repository.UsuarioBotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioBotService {


    private final UsuarioBotRepository usuarioBotRepository;

    public UsuarioBotService(UsuarioBotRepository usuarioBotRepository) {
        this.usuarioBotRepository = usuarioBotRepository;
    }

    public UsuarioBot getOrCreateUsuario(Long waId, String primerNombre) {
        return usuarioBotRepository.findById(waId)
                .map(usuario -> {usuario.setPrimerNombre(primerNombre);
                    return usuarioBotRepository.save(usuario);
                })
                .orElseGet(() -> {
                    UsuarioBot nuevo = new UsuarioBot();
                    nuevo.setWaId(waId);
                    nuevo.setPrimerNombre(primerNombre);
                    nuevo.setEstadoActual(EstadoBot.IDLE);
                    nuevo.setOnboardingEnviado(false);
                    return usuarioBotRepository.save(nuevo);
                });
    }

    @Transactional
    public void marcarOnboardingEnviado(Long waId) {
        UsuarioBot usuario = usuarioBotRepository.findById(waId).orElseThrow();
        usuario.setOnboardingEnviado(true);
        usuarioBotRepository.save(usuario);
    }

    @Transactional
    public void actualizarEstado(Long waId, EstadoBot nuevoEstado) {
        UsuarioBot usuario = usuarioBotRepository.findById(waId).orElseThrow();
        usuario.setEstadoActual(nuevoEstado);
        usuarioBotRepository.save(usuario);
    }
}
