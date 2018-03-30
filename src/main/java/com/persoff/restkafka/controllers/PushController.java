package com.persoff.restkafka.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persoff.restkafka.services.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PushController {

    @Autowired
    KafkaService kafkaHandler;

    /*
    Метод отправки в Kafka
     */
    @RequestMapping(value = "/send/{topic}", method = RequestMethod.POST)
    DeferredResult<ResponseEntity<?>> send(@PathVariable String topic, @RequestBody String inputJson) throws IOException {

        // Парсин JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);
        String key = json.remove("key").asText();
        String message = json.toString();

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        // Проверка соединения и отправка сообщения
        if (!kafkaHandler.testSocket()) {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("SERVERS ARE NOT AVAILABLE", HttpStatus.SERVICE_UNAVAILABLE);
            deferredResult.setResult(responseEntity);
        }
        else {
            String response = kafkaHandler.send(topic, key, message);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
            deferredResult.setResult(responseEntity);
        }

        return deferredResult;
    }

    /*
    Метод чтения из Kafka
     */
    @RequestMapping(value = "/poll/{topic}", method = RequestMethod.POST)
    DeferredResult<ResponseEntity<?>> poll(@PathVariable String topic, @RequestBody String inputJson) throws IOException {

        // Парсинг JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(inputJson);
        String key = json.remove("key").asText();

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        // Проверка соединения и чтение сообщения
        if (!kafkaHandler.testSocket()) {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("SERVERS ARE NOT AVAILABLE", HttpStatus.SERVICE_UNAVAILABLE);
            deferredResult.setResult(responseEntity);
        }
        else {
            List<String> recordsList = kafkaHandler.read(topic, key);
            String recordsString = mapper.writeValueAsString(recordsList.toArray());
            ResponseEntity<String> responseEntity = new ResponseEntity<>(recordsString, HttpStatus.OK);
            deferredResult.setResult(responseEntity);
        }

        return deferredResult;
    }

}
