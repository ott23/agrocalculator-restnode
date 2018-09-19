package net.tngroup.acrestnode.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.tngroup.acrestnode.utils.Json;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Responses {

    public static ResponseEntity okResponse(Object o) {
        try {
            return ResponseEntity.ok(Json.objectMapper.writeValueAsString(o));
        } catch (JsonProcessingException e) {
            return badResponse(e);
        }
    }

    public static ResponseEntity successResponse() {
        ObjectNode jsonResponse = new ObjectMapper().createObjectNode();
        jsonResponse.put("response", "Success");
        String response = jsonResponse.toString();
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity badResponse(Exception e) {
        ObjectNode jsonResponse = new ObjectMapper().createObjectNode();
        jsonResponse.put("response", "Server error: " + e.getMessage());
        String response = jsonResponse.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    public static ResponseEntity nonFoundResponse() {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("response", "Not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.toString());
    }

    public static ResponseEntity conflictResponse(String col) {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("response", "Column conflict - " + col);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response.toString());
    }

    public static ResponseEntity failedDependencyResponse() {
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).build();
    }

    public static ResponseEntity kafkaNotAvailableResponse() {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("response", "Servers are not available");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response.toString());
    }
}
