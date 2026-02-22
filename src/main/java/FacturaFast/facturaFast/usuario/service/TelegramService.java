package FacturaFast.facturaFast.usuario.service;


import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {
    @org.springframework.beans.factory.annotation.Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarMensaje(Long chatId, String texto){

        String url = "https://api.telegram.org/bot" + botToken + "/SendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", texto);
        body.put("parse_mode", "Markdown");

        try {
            restTemplate.postForObject(url, body, String.class);
        } catch (Exception e){
            System.err.println("Error enviando mensaje a telegram: " + e.getMessage());
        }

        //aqui falta lo del pdf porque aun no se como voy a hacerlo XD
    }

}
