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
import java.util.UUID;

import static net.tngroup.acrestnode.web.controllers.Responses.*;

@RestController
@RequestMapping("/task")
public class TaskController {

    public static final int ATTEMPTS_COUNT = 4;

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
            if (!testKafkaConnextion()) return kafkaNotAvailableResponse();

            final String topic = taskRequest.getTopic();
            final String message = taskRequest.getMessage();

            final TaskCondition oldTaskCondition = taskConditionService.getByHashCode(new TaskCondition(topic, message).getHashCode());

            final UUID taskUuid = oldTaskCondition != null
                    && oldTaskCondition.getKey().getClient().equals(client.getId()) ?
                    oldTaskCondition.getKey().getTask() :
                    UUID.randomUUID();


            final String key = client.getId() + "/" + taskUuid;

            // Kafka request
            final String kafkaResponse = kafkaComponent.send(topic, key, message);

            if (kafkaResponse.equals("Success")) {
                final TaskKey taskKey = new TaskKey(client.getId(), taskUuid);
                TaskCondition newTaskCondition = new TaskCondition(taskKey, topic, message);

                newTaskCondition = taskConditionService.save(newTaskCondition);

                for (int i = 0; i < ATTEMPTS_COUNT; i++) {
                    sleep(500 * i);
                    final TaskResult taskResult = taskResultService.getByKey(taskKey);
                    if (taskResult != null && taskResult.getTime().after(newTaskCondition.getTime())) {
                        return okResponse(formTaskResult(jsonComponent, taskResult));
                    }
                }
                return okResponse(formTaskId(taskUuid));
            } else {
                return badResponse(new Exception("TaskCondition wasn't accepted"));
            }
        });


    }


    @RequestMapping(value = "/poll", method = RequestMethod.POST)
    public ResponseEntity poll(HttpServletRequest request, @RequestBody PollRequest pollRequest) {

        return securityComponent.doIfUser(client -> {

            if (!testKafkaConnextion()) return kafkaNotAvailableResponse();

            if (pollRequest.getTask() == null) return badResponse(new Exception("task must be not null"));

            final TaskKey taskKey = new TaskKey(client.getId(), pollRequest.getTask());

            if (!taskConditionService.getByTaskKey(taskKey).isPresent()) return nonFoundResponse();

            final TaskResult taskResult = taskResultService.getByKey(taskKey);

            if (taskResult == null) return okResponse(formTaskNotReady(jsonComponent, pollRequest.getTask()));
            else return okResponse(formTaskResult(jsonComponent, taskResult));
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

    public static String formTaskResult(JsonComponent jsonComponent, TaskResult taskResult) {
        ObjectMapper mapper = jsonComponent.getObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        response.put("task", taskResult.getKey().getTask().toString());
        response.put("time", taskResult.getTime().getTime());
        response.putPOJO("result", taskResult.getValue());

        return response.toString();
    }

    public static String formTaskNotReady(JsonComponent jsonComponent, UUID task) {
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