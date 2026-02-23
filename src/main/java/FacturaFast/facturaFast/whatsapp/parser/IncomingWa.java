package FacturaFast.facturaFast.whatsapp.parser;

public record IncomingWa(String waId, String profileName, String textBody, String buttonId) {
}
