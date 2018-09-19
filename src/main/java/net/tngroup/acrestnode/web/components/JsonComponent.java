package net.tngroup.acrestnode.web.components;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import net.tngroup.acrestnode.utils.Json;
import org.springframework.stereotype.Component;

@Component
public class JsonComponent {

    @Getter
    private ObjectMapper objectMapper;

    public JsonComponent() {
        objectMapper = Json.objectMapper;
    }

}
