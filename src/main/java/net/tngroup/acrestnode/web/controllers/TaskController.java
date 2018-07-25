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
import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
import net.tngroup.acrestnode.web.components.KafkaComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    // Loggers
    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private ChannelComponent channelComponent;
    private KafkaComponent kafkaComponent;
    private ClientService clientService;
    private TaskConditionService taskConditionService;
    private TaskResultService taskResultService;

    @Autowired
    public TaskController(ChannelComponent channelComponent,
                          @Lazy KafkaComponent kafkaComponent,
                          @Lazy ClientService clientService,
                          @Lazy TaskConditionService taskConditionService,
                          @Lazy TaskResultService taskResultService) {
        this.channelComponent = channelComponent;
        this.kafkaComponent = kafkaComponent;
        this.clientService = clientService;
        this.taskConditionService = taskConditionService;
        this.taskResultService = taskResultService;
    }

    /*
    Метод отправки в Kafka
     */
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity send(@RequestBody String inputJson, HttpServletRequest request) throws IOException {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Logging
        logger.info("Request to `/task/send` (add task) from " + request.getRemoteAddr() + " by `" + client.getName() + "`");

        // Error if channel is closed
        if (!channelComponent.isStatus()) return badResponse(new Exception("Server is not started"));

        // Connection with Kafka testing
        try {
            kafkaComponent.testSocket();
        } catch (Exception e) {
            return kafkaNotAvailableResponse();
        }

        // Json parsing
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);
        String topic = json.remove("topic").asText();

        UUID task = UUID.randomUUID();

        String message = json.toString();
        String key = client.getId() + "/" + task;

        // Kafka request
        String kafkaResponse = kafkaComponent.send(topic, key, message);

        if (kafkaResponse.equals("Success")) {
            TaskKey taskKey = new TaskKey(client.getId(), task);
            TaskCondition taskCondition = new TaskCondition(taskKey, topic, message);
            taskConditionService.save(taskCondition);
            return okResponse(task);
        } else {
            return badResponse(new Exception("TaskCondition wasn't accepted"));
        }

    }

    /*
    Методы чтения из Kafka
     */
    /*
    @RequestMapping(value = "/poll", method = RequestMethod.POST)
    public ResponseEntity poll(@RequestBody String inputJson) throws IOException {
        if (!channelComponent.isStatus()) return badResponse(new Exception("Server is not started"));

        // Connection with Kafka testing
        try {
            kafkaComponent.testSocket();
        } catch (Exception e) {
            return kafkaNotAvailableResponse();
        }

        // Json parsing
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree(inputJson);
        String clientId = json.remove("clientId").asText();

        // Kafka request
        Map<String, String> taskMap = kafkaComponent.read(clientId, false);

        // Answer preparing
        List<String> taskList = new ArrayList<>();
        taskMap.forEach((key, value) -> taskList.add(value));

        return okResponse(formTaskListResponse(taskList));
    }*/


    @RequestMapping(value = "/poll", method = RequestMethod.POST)
    public ResponseEntity pollTaskId(@RequestBody String inputJson, HttpServletRequest request) throws IOException {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Logging
        logger.info("Request to `/task/poll` (read task) from " + request.getRemoteAddr() + " by `" + client.getName() + "`");

        // Error if channel is closed
        if (!channelComponent.isStatus()) return badResponse(new Exception("Server is not started"));

        // Connection with Kafka testing
        try {
            kafkaComponent.testSocket();
        } catch (Exception e) {
            return kafkaNotAvailableResponse();
        }

        // Json parsing
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree(inputJson);
        UUID task = UUID.fromString(json.remove("task").asText());

        TaskKey taskKey = new TaskKey(client.getId(), task);
        TaskResult taskResult = taskResultService.getByKey(taskKey);

        return okResponse(formTaskResult(taskResult));
    }

    private String formTaskResult(TaskResult taskResult) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        if (taskResult == null) response.put("result", "No result available");
        else response.put("result", taskResult.getValue());

        return response.toString();
    }


    /*
    private String formTaskListResponse(List<String> taskList) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        ArrayNode tasks = mapper.createArrayNode();

        taskList.forEach(tasks::addPOJO);

        response.putPOJO("response", tasks);
        response.put("size", taskList.size());

        return response.toString();
    }
    */

}