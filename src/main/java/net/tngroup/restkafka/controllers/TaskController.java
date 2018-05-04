package net.tngroup.restkafka.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.tngroup.restkafka.services.ConfigService;
import net.tngroup.restkafka.services.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    KafkaService kafkaService;

    @Autowired
    ConfigService configService;

    private String resultsTopicPrefix;
    private int maxCount;

    @PostConstruct
    public void init() {
        resultsTopicPrefix = configService.getProperty("tn.kafka.results-topic-prefix");
        maxCount = Integer.parseInt(configService.getProperty("tn.kafka.max-message-size"));
    }

    /*
    Метод отправки в Kafka
     */
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    DeferredResult<ResponseEntity<?>> send(@RequestBody String inputJson) throws IOException {

        // Парсин JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);
        String topicId = json.get("topicId").asText();
        String clientId = json.get("clientId").asText();
        String taskId = json.get("taskId").asText();
        String message = json.toString();

        String key = clientId + "-" + taskId;

        // Проверка соединения и отправка сообщения
        if (kafkaService.isKafkaNotAvailable())
            return kafkaNotAvailableResponse();

        String kafkaResponse = kafkaService.send(topicId, key, message);

        ObjectNode responseJson = mapper.createObjectNode();
        responseJson.put("response", kafkaResponse);
        String response = responseJson.toString();

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        ResponseEntity<String> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        deferredResult.setResult(responseEntity);

        return deferredResult;

    }

    /*
    Методы чтения из Kafka
     */
    @RequestMapping(value = "/poll", method = RequestMethod.POST)
    DeferredResult<ResponseEntity<?>> poll(@RequestBody String inputJson) throws IOException {

        // Парсин JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);
        String clientId = json.remove("clientId").asText();
        String topicId = resultsTopicPrefix + clientId;

        // Проверка соединения и чтение сообщения
        if (kafkaService.isKafkaNotAvailable())
            return kafkaNotAvailableResponse();

        Map<String, String> taskMap = kafkaService.read(topicId, clientId, false, maxCount);

        List<String> taskList = new ArrayList<>();

        taskMap.forEach((key, value) -> taskList.add(value));

        ResponseEntity<String> responseEntity = new ResponseEntity<>(makeResponse(taskList), HttpStatus.OK);
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        deferredResult.setResult(responseEntity);

        return deferredResult;
    }


    @RequestMapping(value = "/poll/task-id", method = RequestMethod.POST)
    DeferredResult<ResponseEntity<?>> pollTaskId(@RequestBody String inputJson) throws IOException {

        // Парсин JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);
        String clientId = json.remove("clientId").asText();
        String taskId = json.remove("taskId").asText();

        String topic = resultsTopicPrefix + clientId;

        // Проверка соединения и чтение сообщения
        if (kafkaService.isKafkaNotAvailable())
            return kafkaNotAvailableResponse();

        Map<String, String> taskMap = kafkaService.read(topic, clientId, true, 0);

        List<String> taskList = new ArrayList<>();

        taskMap.forEach((key, value) -> {
            if (key.split("-")[1].equals(taskId))
                taskList.add(value);
        });

        ResponseEntity<String> responseEntity = new ResponseEntity<>(makeResponse(taskList), HttpStatus.OK);
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        deferredResult.setResult(responseEntity);

        return deferredResult;
    }

    private DeferredResult<ResponseEntity<?>> kafkaNotAvailableResponse() {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("response", "Servers are not available");
        ResponseEntity<String> responseEntity = new ResponseEntity<>(response.toString(), HttpStatus.SERVICE_UNAVAILABLE);
        deferredResult.setResult(responseEntity);
        return deferredResult;
    }

    private String makeResponse(List<String> taskList) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        ArrayNode tasks = mapper.createArrayNode();

        for (String task : taskList) {
            tasks.addPOJO(task);
        }

        response.putPOJO("response", tasks);
        response.put("size", taskList.size());

        return response.toString();
    }

}