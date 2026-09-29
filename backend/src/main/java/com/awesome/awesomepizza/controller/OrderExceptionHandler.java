package com.awesome.awesomepizza.controller;

import com.awesome.awesomepizza.service.OrderConflictException;
import com.awesome.awesomepizza.service.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(OrderNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({OrderConflictException.class,
            ObjectOptimisticLockingFailureException.class,
            PessimisticLockingFailureException.class})
    ResponseEntity<ProblemDetail> conflict(Exception exception) {
        return problem(HttpStatus.CONFLICT, exception instanceof OrderConflictException
                ? exception.getMessage() : "Concurrent update: reload the order and try again");
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class})
    ResponseEntity<ProblemDetail> invalidInput(Exception exception) {
        String detail = exception instanceof HttpMessageNotReadableException
                ? "Request body must contain valid JSON"
                : exception instanceof MethodArgumentNotValidException
                ? "Pizza type is required and must be at most 255 characters"
                : exception.getMessage();
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        return ResponseEntity.status(status).body(body);
    }
}
