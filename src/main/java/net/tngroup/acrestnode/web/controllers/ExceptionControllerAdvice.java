package net.tngroup.acrestnode.web.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(HttpServletRequest req, Exception e) {
        e.printStackTrace();
        return Responses.badResponse(e);
    }

}
