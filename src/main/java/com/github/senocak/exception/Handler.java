package com.github.senocak.exception;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import com.github.senocak.payload.ResponseSchema;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class Handler extends ResponseEntityExceptionHandler {
    private static final ResponseSchema RESPONSE_SCHEMA = new ResponseSchema(false, null);

    @ExceptionHandler(value = { BadCredentialsException.class })
    protected ResponseEntity<Object> handleBadCredentialsException(RuntimeException ex) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = { org.springframework.security.access.AccessDeniedException.class })
    protected ResponseEntity<Object> handleAccessDeniedException(RuntimeException ex) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(value = { ServerException.class })
    protected ResponseEntity<Object> handleServerException(Exception ex) {
        List<String> response = new ArrayList<>();
        response.add(((ServerException) ex).getErrorMessageType().getMessageId());
        response.add(((ServerException) ex).getErrorMessageType().getText());
        if (!Objects.isNull(((ServerException) ex).getVariables()))
            response.addAll(Arrays.asList(((ServerException) ex).getVariables()));
        RESPONSE_SCHEMA.setMessage(response);
        return generateResponseEntity(((ServerException) ex).getStatusCode());
    }
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.BAD_REQUEST);
    }
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleGeneralException(Exception ex) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.NOT_FOUND);
    }
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.BAD_REQUEST);
    }
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.BAD_REQUEST);
    }
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        return generateResponseEntity(HttpStatus.BAD_REQUEST);
    }
    private ResponseEntity<Object> generateResponseEntity(HttpStatus httpStatus){
        log.error("Exception is handled. Exception: {}, HttpStatus: {}", Handler.RESPONSE_SCHEMA.getMessage(), httpStatus);
        return new ResponseEntity<>(Handler.RESPONSE_SCHEMA, httpStatus);
    }
}
