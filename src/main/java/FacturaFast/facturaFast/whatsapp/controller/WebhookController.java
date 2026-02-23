package FacturaFast.facturaFast.whatsapp.controller;

import FacturaFast.facturaFast.whatsapp.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class WebhookController {

    @Value("${meta.verify_token}")
    private String verifyToken;

    private final WhatsAppService whatsAppService;

    public WebhookController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    // Meta webhook verification (GET)
    @GetMapping("/webhook")
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge
    ) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Forbidden");
    }

    // Incoming messages (POST)
    @PostMapping("/webhook")
    public ResponseEntity<String> receive(@RequestBody Map<String, Object> payload) {
        System.out.println("WEBHOOK IN: " + payload);
        whatsAppService.tryAutoReply(payload);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}