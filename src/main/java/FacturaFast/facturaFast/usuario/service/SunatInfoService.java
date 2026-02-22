package FacturaFast.facturaFast.usuario.service;


import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class SunatInfoService {

    @Data
    @Builder
    public static class InfoEmpresa {
        private String razonSocial;
        private String direccion;
        private String ubigeo; // Ej: 150101
    }


    public InfoEmpresa consultarRuc(String ruc){
        return InfoEmpresa.builder().razonSocial("Empresa Nombre")
                .direccion("AV lugar")
                .ubigeo("150101")
                .build();

    }
}
