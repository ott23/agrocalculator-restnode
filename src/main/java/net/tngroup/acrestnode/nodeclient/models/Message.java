package net.tngroup.acrestnode.nodeclient.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.io.IOException;

@Data
public class Message {

    private String code;

    private String type;

    private String value;

    private Integer id;

    private boolean encoded = false;

    public Message(String code, String type, String value, Integer id) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.id = id;
    }

    public Message(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonInput = objectMapper.readTree(message);
        code = jsonInput.get("code").asText();
        type = jsonInput.get("type").asText();
        value = jsonInput.get("value").asText();
        id = jsonInput.get("id").asInt();
        encoded = jsonInput.get("encoded").asBoolean();
    }

    public String formJson() {
        ObjectNode json = new ObjectMapper().createObjectNode();
        json.put("code", code);
        json.put("type", type);
        json.put("value", value);
        json.put("id", id);
        json.put("encoded", encoded);
        return json.toString();
    }
}
