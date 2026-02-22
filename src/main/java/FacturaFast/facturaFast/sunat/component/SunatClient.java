package FacturaFast.facturaFast.sunat.component;

import FacturaFast.facturaFast.empresa.entity.Empresa;
import FacturaFast.facturaFast.sunat.dto.SunatResponse;
import jakarta.xml.soap.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SunatClient {

    @Value("${sunat.endpoint:https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService}")
    private String sunatEndpoint;

    public SunatResponse sendBill(String fileName, byte[] xmlFirmadoZip, Empresa empresa){
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();


            return SunatResponse.builder()
                    .success(true)
                    .errorMessage(null)
                    .build();

        } catch (Exception e){
            e.printStackTrace();
            return SunatResponse.builder()
                    .success(false)
                    .errorMessage("Error conexion: " + e.getMessage())
                    .build();
        }
    }


    private void agregarSeguridad(SOAPMessage soapMessage, String usuario, String clave) throws SOAPException{
        SOAPHeader header = soapMessage.getSOAPHeader();

        String wssePrefix = "wsse";
        String wsseNameSpace = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
        SOAPElement security = header.addChildElement("Security", wssePrefix, wsseNameSpace);

        SOAPElement usernameToken = security.addChildElement("UsernameToken", wssePrefix);
        usernameToken.addChildElement("Username", wssePrefix).addTextNode(usuario);
        usernameToken.addChildElement("Password", wssePrefix).addTextNode(clave);
    }

}
