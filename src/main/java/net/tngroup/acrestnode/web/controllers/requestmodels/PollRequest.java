package net.tngroup.acrestnode.web.controllers.requestmodels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

public class PollRequest {

    @JsonProperty("task")
    @Getter
    private UUID task;
}
