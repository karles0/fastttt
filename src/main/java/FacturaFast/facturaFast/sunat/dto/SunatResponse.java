package FacturaFast.facturaFast.sunat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SunatResponse {

    private boolean success;
    private String ticket;
    private byte[] crdZip;
    private String errorMessage;
    private String errorCode;

}
