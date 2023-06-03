package com.akatsuki.reservation.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Set;

@RestControllerAdvice
public class ControllerAdvicer {

    @ExceptionHandler({RuntimeException.class, BadRequestException.class})
    public ResponseEntity<ResponseMessageDto> handleRuntimeException(
            final RuntimeException ex) {
        return new ResponseEntity<>(createResponseMessage(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessageDto handleMethodArgNotValid(MethodArgumentNotValidException exception) {
        return new ResponseMessageDto(LocalDateTime.now(), exception.getLocalizedMessage());
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseMessageDto handleResourceNotFoundException(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        StringBuilder exceptionMessage = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            exceptionMessage.append(violation.getMessageTemplate());
        }
        return new ResponseMessageDto(LocalDateTime.now(), exceptionMessage.toString());
    }

    private ResponseMessageDto createResponseMessage(final String message) {
        return ResponseMessageDto.builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
    }
}
