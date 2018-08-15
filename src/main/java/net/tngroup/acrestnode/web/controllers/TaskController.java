package net.tngroup.acrestnode.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.models.TaskResult;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskConditionService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskResultService;
import net.tngroup.acrestnode.web.components.KafkaComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

import static net.tngroup.acrestnode.web.controllers.Responses.*;

@RestController
@RequestMapping("/task")
public class TaskController {

    private KafkaComponent kafkaComponent;
    private ClientService clientService;
    private TaskConditionService taskConditionService;
    private TaskResultService taskResultService;

    @Autowired
    public TaskController(@Lazy KafkaComponent kafkaComponent,
                          @Lazy ClientService clientService,
                          @Lazy TaskConditionService taskConditionService,
                          @Lazy TaskResultService taskResultService) {
        this.kafkaComponent = kafkaComponent;
        this.clientService = clientService;
        this.taskConditionService = taskConditionService;
        this.taskResultService = taskResultService;
    }

    /*
    Метод отправки в Kafka
     */
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity send(@RequestBody String jsonRequest, HttpServletRequest request) throws IOException {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Connection with Kafka testing
        try {
            kafkaComponent.testSocket();
        } catch (Exception e) {
            return kafkaNotAvailableResponse();
        }

        // Json parsing
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(jsonRequest);
        String topic = json.remove("topic").asText();
        String message = json.toString();

        TaskCondition oldTaskCondition = taskConditionService.getByHashCode(new TaskCondition(topic, message).getHashCode());

        UUID task;
        if (oldTaskCondition != null && oldTaskCondition.getKey().getClient().equals(client.getId())) {
            task = oldTaskCondition.getKey().getTask();
        } else {
            task = UUID.randomUUID();
        }

        String key = client.getId() + "/" + task;

        // Kafka request
        String kafkaResponse = kafkaComponent.send(topic, key, message);

        if (kafkaResponse.equals("Success")) {
            TaskKey taskKey = new TaskKey(client.getId(), task);
            TaskCondition newTaskCondition = new TaskCondition(taskKey, topic, message);
            taskConditionService.save(newTaskCondition);

            for (int i = 0; i < 4; i++) {
                sleep(500 * i);
                TaskResult taskResult = taskResultService.getByKey(taskKey);
                if (taskResult != null && taskResult.getTime().compareTo(newTaskCondition.getTime()) > 0) {
                    return okResponse(formTaskResult(taskResult));
                }
            }
            return okResponse(formTaskId(task));
        } else {
            return badResponse(new Exception("TaskCondition wasn't accepted"));
        }

    }


    @RequestMapping(value = "/poll", method = RequestMethod.POST)
    public ResponseEntity poll(@RequestBody String jsonRequest, HttpServletRequest request) throws IOException {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Connection with Kafka testing
        try {
            kafkaComponent.testSocket();
        } catch (Exception e) {
            return kafkaNotAvailableResponse();
        }

        // Json parsing
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree(jsonRequest);
        UUID task = UUID.fromString(json.remove("task").asText());

        TaskKey taskKey = new TaskKey(client.getId(), task);
        TaskResult taskResult = taskResultService.getByKey(taskKey);

        if (taskResult == null) return okResponse(formTaskNotReady(task));
        else return okResponse(formTaskResult(taskResult));
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String formTaskId(UUID task) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        response.put("task", task.toString());

        return response.toString();
    }

    private String formTaskResult(TaskResult taskResult) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        response.put("task", taskResult.getKey().getTask().toString());
        response.put("time", taskResult.getTime().getTime());
        response.putPOJO("result", taskResult.getValue());

        return response.toString();
    }

    private String formTaskNotReady(UUID task) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        response.put("task", task.toString());
        response.put("result", "No result available");

        return response.toString();
    }
}