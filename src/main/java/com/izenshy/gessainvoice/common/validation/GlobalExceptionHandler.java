package com.izenshy.gessainvoice.common.validation;

import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.common.response.ResourceAlreadyExistsException;
import com.izenshy.gessainvoice.common.response.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<GessaApiResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GessaApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GessaApiResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GessaApiResponse.error(ex.getMessage()));
    }
}
