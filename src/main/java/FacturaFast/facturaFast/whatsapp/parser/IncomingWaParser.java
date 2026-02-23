package FacturaFast.facturaFast.whatsapp.parser;

import java.util.List;
import java.util.Map;

public final class IncomingWaParser {

    private IncomingWaParser() {
    }

    @SuppressWarnings("unchecked")
    public static IncomingWa parse(Map<String, Object> payload) {
        try {
            List<Map<String, Object>> entry = (List<Map<String, Object>>) payload.get("entry");
            if (entry == null || entry.isEmpty()) return null;
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get(0).get("changes");
            if (changes == null || changes.isEmpty()) return null;
            Map<String, Object> value = (Map<String, Object>) changes.get(0).get("value");
            if (value == null) return null;

            List<Map<String, Object>> contacts = (List<Map<String, Object>>) value.get("contacts");
            String waId = null;
            String profileName = "";
            if (contacts != null && !contacts.isEmpty()) {
                waId = (String) contacts.get(0).get("wa_id");
                Map<String, Object> profile = (Map<String, Object>) contacts.get(0).get("profile");
                if (profile != null && profile.get("name") != null) {
                    profileName = profile.get("name").toString();
                }
            }

            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            if (messages == null || messages.isEmpty()) return null;
            Map<String, Object> msg = messages.get(0);

            String textBody = null;
            String buttonId = null;

            Map<String, Object> text = (Map<String, Object>) msg.get("text");
            if (text != null && text.get("body") != null) {
                textBody = text.get("body").toString();
            }

            Map<String, Object> interactive = (Map<String, Object>) msg.get("interactive");
            if (interactive != null) {
                Map<String, Object> buttonReply = (Map<String, Object>) interactive.get("button_reply");
                if (buttonReply != null && buttonReply.get("id") != null) {
                    buttonId = buttonReply.get("id").toString();
                }
            }

            if (waId == null) {
                Object from = msg.get("from");
                waId = from != null ? from.toString() : null;
            }

            return new IncomingWa(waId, profileName, textBody, buttonId);
        } catch (Exception e) {
            return null;
        }
    }
}
