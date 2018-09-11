package net.tngroup.acrestnode.web.components;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonComponent {


    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }



    @Autowired
    public JsonComponent() {
        objectMapper = new ObjectMapper();
    }

}
