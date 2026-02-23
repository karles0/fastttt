package FacturaFast.facturaFast.whatsapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class WhatsAppApiClient {

    @Value("${meta.access_token:}")
    private String accessToken;

    @Value("${meta.phone_number_id:}")
    private String phoneNumberId;

    @Value("${meta.graph_version:}")
    private String graphVersion;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendText(String waId, String text) {
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", waId,
                "type", "text",
                "text", Map.of("body", text)
        );
        post(body);
    }

    public void sendButtons(String waId, String bodyText, String footer, Map<String, String> buttons) {
        List<Map<String, Object>> actionButtons = buttons.entrySet().stream()
                .limit(3)
                .map(entry -> Map.of(
                        "type", "reply",
                        "reply", Map.of("id", entry.getKey(), "title", entry.getValue())
                )).toList();

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", waId,
                "type", "interactive",
                "interactive", Map.of(
                        "type", "button",
                        "body", Map.of("text", bodyText),
                        "footer", Map.of("text", footer),
                        "action", Map.of("buttons", actionButtons)
                )
        );
        post(body);
    }

    private void post(Map<String, Object> payload) {
        if (accessToken == null || accessToken.isBlank() || phoneNumberId == null || phoneNumberId.isBlank()) {
            return;
        }
        String url = "https://graph.facebook.com/" + graphVersion + "/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
    }
}
