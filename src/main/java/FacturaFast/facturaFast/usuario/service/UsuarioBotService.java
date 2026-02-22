package FacturaFast.facturaFast.usuario.service;

import FacturaFast.facturaFast.usuario.entity.EstadoBot;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import FacturaFast.facturaFast.usuario.repository.UsuarioBotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioBotService {


    private final UsuarioBotRepository usuarioBotRepository;

    public UsuarioBotService(UsuarioBotRepository usuarioRepo) {
        this.usuarioBotRepository = usuarioRepo;
    }

    @Transactional
    public UsuarioBot getOrCreateUsuario(Long telegramId, String firstName, String userName){
        return usuarioRepo.findById(telegramId)
                .map(usuario -> {
                    usuario.setPrimerNombre(firstName);
                    usuario.setUserName(userName);
                    return usuarioRepo.save(usuario);
                })
                .orElseGet(() -> {
                    UsuarioBot nuevo = new UsuarioBot();
                    nuevo.setTelegramId(telegramId);
                    nuevo.setPrimerNombre(firstName);
                    nuevo.setUserName(userName);
                    nuevo.setEstadoActual(EstadoBot.IDLE);
                    return usuarioRepo.save(nuevo);
                });

    }

    @Transactional
    public void actualizarEstado(Long telegramId, EstadoBot nuevoEstado){
        UsuarioBot usuario = usuarioRepo.findById(telegramId).orElseThrow();
        usuario.setEstadoActual(nuevoEstado);
        usuarioRepo.save(usuario);
    }


}
