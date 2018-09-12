package net.tngroup.acrestnode.web.components;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class JsonComponent {

    @Getter
    private ObjectMapper objectMapper;

    public JsonComponent() {
        objectMapper = new ObjectMapper();
    }

}
