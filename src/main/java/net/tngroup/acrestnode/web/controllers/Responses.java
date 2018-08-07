package net.tngroup.acrestnode.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class Responses {

    static ResponseEntity okResponse(Object o) {
        return ResponseEntity.ok(o);
    }

    static ResponseEntity successResponse() {
        ObjectNode jsonResponse = new ObjectMapper().createObjectNode();
        jsonResponse.put("response", "Success");
        String response = jsonResponse.toString();
        return ResponseEntity.ok(response);
    }

    static ResponseEntity badResponse(Exception e) {
        ObjectNode jsonResponse = new ObjectMapper().createObjectNode();
        jsonResponse.put("response", "Server error: " + e.getMessage());
        String response = jsonResponse.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    static ResponseEntity nonFoundResponse() {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("response", "Not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.toString());
    }

    static ResponseEntity conflictResponse(String col) {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("response", "Column conflict - " + col);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response.toString());
    }

    static ResponseEntity failedDependencyResponse() {
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).build();
    }

    static ResponseEntity kafkaNotAvailableResponse() {
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("response", "Servers are not available");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response.toString());
    }
}
