package net.tngroup.acrestnode.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.models.TaskResult;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskConditionService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskResultService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.components.KafkaComponent;
import net.tngroup.acrestnode.web.controllers.requestmodels.PollRequest;
import net.tngroup.acrestnode.web.controllers.requestmodels.TaskRequest;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
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
    private JsonComponent jsonComponent;
    private SecurityComponent securityComponent;

    @Autowired
    public TaskController(@Lazy KafkaComponent kafkaComponent,
                          @Lazy ClientService clientService,
                          @Lazy TaskConditionService taskConditionService,
                          @Lazy TaskResultService taskResultService,
                          JsonComponent jsonComponent,
                          SecurityComponent securityComponent) {
        this.kafkaComponent = kafkaComponent;
        this.clientService = clientService;
        this.taskConditionService = taskConditionService;
        this.taskResultService = taskResultService;
        this.jsonComponent = jsonComponent;
        this.securityComponent = securityComponent;
    }

    /*
    Метод отправки в Kafka
     */
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity send(HttpServletRequest request, @RequestBody TaskRequest taskRequest) {

        return securityComponent.doIfUser(client -> {

            // Connection with Kafka testing
            if (testKafkaConnextion()) return kafkaNotAvailableResponse();

            final String topic = taskRequest.getTopic();
            final String message = taskRequest.getMessage();

            final TaskCondition oldTaskCondition = taskConditionService.getByHashCode(new TaskCondition(topic, message).getHashCode());

            final UUID task;
            if (oldTaskCondition != null && oldTaskCondition.getKey().getClient().equals(client.getId())) {
                task = oldTaskCondition.getKey().getTask();
            } else {
                task = UUID.randomUUID();
            }

            final String key = client.getId() + "/" + task;

            // Kafka request
            final String kafkaResponse = kafkaComponent.send(topic, key, message);

            if (kafkaResponse.equals("Success")) {
                final TaskKey taskKey = new TaskKey(client.getId(), task);
                final TaskCondition newTaskCondition = new TaskCondition(taskKey, topic, message);
                taskConditionService.save(newTaskCondition);

                for (int i = 0; i < 4; i++) {
                    sleep(500 * i);
                    final TaskResult taskResult = taskResultService.getByKey(taskKey);
                    if (taskResult != null && taskResult.getTime().compareTo(newTaskCondition.getTime()) > 0) {
                        return okResponse(formTaskResult(taskResult));
                    }
                }
                return okResponse(formTaskId(task));
            } else {
                return badResponse(new Exception("TaskCondition wasn't accepted"));
            }
        });


    }


    @RequestMapping(value = "/poll", method = RequestMethod.POST)
    public ResponseEntity poll(HttpServletRequest request, @RequestBody PollRequest pollRequest) throws IOException {

        return securityComponent.doIfUser(client -> {
            if (testKafkaConnextion()) return kafkaNotAvailableResponse();

            final TaskKey taskKey = new TaskKey(client.getId(), pollRequest.getTask());
            final TaskResult taskResult = taskResultService.getByKey(taskKey);

            if (taskResult == null) return okResponse(formTaskNotReady(pollRequest.getTask()));
            else return okResponse(formTaskResult(taskResult));
        });
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String formTaskId(UUID task) {
        ObjectMapper mapper = jsonComponent.getObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        response.put("task", task.toString());

        return response.toString();
    }

    private String formTaskResult(TaskResult taskResult) {
        ObjectMapper mapper = jsonComponent.getObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        response.put("task", taskResult.getKey().getTask().toString());
        response.put("time", taskResult.getTime().getTime());
        response.putPOJO("result", taskResult.getValue());

        return response.toString();
    }

    private String formTaskNotReady(UUID task) {
        ObjectMapper mapper = jsonComponent.getObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        response.put("task", task.toString());
        response.put("result", "No result available");

        return response.toString();
    }

    private boolean testKafkaConnextion() {
        try {
            kafkaComponent.testSocket();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}