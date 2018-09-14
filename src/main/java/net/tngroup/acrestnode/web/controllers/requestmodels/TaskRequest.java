package net.tngroup.acrestnode.web.controllers.requestmodels;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;


@JsonDeserialize(using = TaskRequestDeserializer.class)
@Data
public class TaskRequest {

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("message")
    private String message;
}
