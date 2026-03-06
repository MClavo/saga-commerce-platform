package com.mclavo.ecommerce.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mclavo.ecommerce.exception.CustomerNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<String> handle(
            CustomerNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetailResponse> handle(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors()
            .forEach(e -> {
                String fieldName = ((FieldError)e).getField();
                String message = e.getDefaultMessage();
                errors.put(fieldName, message);
            });
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorDetailResponse(errors));
    }

}
