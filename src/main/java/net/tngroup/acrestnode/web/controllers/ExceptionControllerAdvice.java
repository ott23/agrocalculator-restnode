package net.tngroup.acrestnode.web.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(HttpServletRequest req, Exception e) {
        e.printStackTrace();
        return Responses.badResponse(e);
    }

}
