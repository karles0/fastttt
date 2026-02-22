package FacturaFast.facturaFast.empresa.service;

import FacturaFast.facturaFast.empresa.entity.Empresa;
import FacturaFast.facturaFast.empresa.repository.EmpresaRepository;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import FacturaFast.facturaFast.usuario.repository.UsuarioBotRepository;
import FacturaFast.facturaFast.usuario.service.SunatInfoService;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioBotRepository usuarioBotRepository;
    private final SunatInfoService sunatInfoService;

    public EmpresaService(EmpresaRepository empresaRepository, UsuarioBotRepository usuarioBotRepository,
                          SunatInfoService sunatInfoService){
        this.empresaRepository = empresaRepository;
        this.usuarioBotRepository = usuarioBotRepository;
        this.sunatInfoService = sunatInfoService;
    }
    public Empresa obtenerEmpresaUsuario(Long telegramId){
        UsuarioBot usuario = usuarioBotRepository.findById(telegramId)
                .orElseThrow(()-> new RuntimeException("Usuario no registrado"));
        Empresa empresa = usuario.getEmpresaSeleccionada();
        if(empresa == null){
            throw new RuntimeException("Este usuario no tiene asignada una empresa para facturar"); //aqui esta lo que quiero hacer
        } //que nosotros sabemos si el usuario ha pagado si es que esta en nuestra base de datos, en donde nosotros lo pondremos
        //
        return empresa;
    }


    public Empresa registrarNuevaEmpresa(String ruc,
                                         String usuarioSol,
                                         String claveSol,
                                         String rutaCertificado, // Archivo .p12 subido
                                         String claveCertificado) {

        // 1. Validar si ya existe
        if (empresaRepository.findByRuc(ruc).isPresent()) {
            throw new RuntimeException("Esta empresa ya está registrada en nuestro sistema.");
        }

        // 2. CONSULTAR DATOS AUTOMÁTICAMENTE (La magia)
        SunatInfoService.InfoEmpresa info = sunatInfoService.consultarRuc(ruc);

        // 3. Crear y Guardar
        Empresa nueva = Empresa.builder()
                .ruc(ruc)
                .razonSocial(info.getRazonSocial()) // Auto-completado
                .direccion(info.getDireccion())     // Auto-completado
                .ubigeo(info.getUbigeo())           // Auto-completado
                .usuarioSol(usuarioSol)
                .claveSol(claveSol)
                .rutaCertificado(rutaCertificado)
                .claveCertificado(claveCertificado)
                .isProduction(true)
                .build();

        return empresaRepository.save(nueva);
    }

}
