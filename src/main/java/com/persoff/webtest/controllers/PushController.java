package com.persoff.webtest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persoff.webtest.services.KafkaHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PushController {

    Logger LOG = LoggerFactory.getLogger(PushController.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Autowired
    KafkaHandler kafkaHandler;

    @RequestMapping(value = "/add-task", method = RequestMethod.POST)
    DeferredResult<ResponseEntity<?>> addTask(@RequestBody String inputJson) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);

        String topic = json.remove("topic").asText();
        String key = json.remove("key").asText();
        String message = json.toString();

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        String response = kafkaHandler.send(topic, key, message);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        deferredResult.setResult(responseEntity);

        return deferredResult;
    }

    @RequestMapping(value = "/read-task", method = RequestMethod.POST)
    DeferredResult<ResponseEntity<?>> readTask(@RequestBody String inputJson) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);

        String topic = json.remove("topic").asText();
        String key = json.remove("key").asText();

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        List<String> recordsList = kafkaHandler.read(topic, key);
        String recordsString = mapper.writeValueAsString(recordsList.toArray());
        ResponseEntity<String> responseEntity = new ResponseEntity<>(recordsString, HttpStatus.OK);
        deferredResult.setResult(responseEntity);

        return deferredResult;
    }

}
