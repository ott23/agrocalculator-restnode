package net.tngroup.acrestnode.web.controllers.requestmodels;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class TaskRequestDeserializer extends StdDeserializer<TaskRequest> {

    public TaskRequestDeserializer() {
        this(null);
    }

    public TaskRequestDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TaskRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        final ObjectNode json = jsonParser.readValueAsTree();

        final TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTopic(json.remove("topic").asText());
        taskRequest.setMessage(json.toString());

        return taskRequest;
    }
}
