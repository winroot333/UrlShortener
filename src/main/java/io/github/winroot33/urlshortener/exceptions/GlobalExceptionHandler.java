package io.github.winroot33.urlshortener.exceptions;

import io.github.winroot33.urlshortener.dtos.ResponseError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * Глобальный Advice для обработки исключений
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.info("Handler method argument error: {}", e.getMessage());
        List<String> errors = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();

        return new ResponseError(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.info("Handler method validation error: {}", e.getMessage());
        List<String> errors = e.getParameterValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();

        return new ResponseError(errors.isEmpty() ? List.of(e.getMessage()) : errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleConstraintViolation(ConstraintViolationException e) {
        log.info("Constraint violation error: {}", e.getMessage());

        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        return new ResponseError(errors);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            LinkAlreadyExistsException.class,

    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleBadRequest(Exception e) {
        log.info(e.getMessage());
        return new ResponseError(e.getMessage());
    }

    @ExceptionHandler(LinkExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ResponseError handleExpired(Exception e) {
        log.info(e.getMessage());
        return new ResponseError(e.getMessage());
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            LinkNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError handleNotFound(Exception e) {
        log.info(e.getMessage());
        return new ResponseError(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError handleAll(Exception e) {
        log.error(e.getMessage());
        return new ResponseError("Internal Server error");

    }

}
