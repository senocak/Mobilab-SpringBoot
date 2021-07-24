package com.github.senocak.exception;

import com.github.senocak.util.OmaErrorMessageType;
import com.github.senocak.payload.ResponseSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;

class HandlerTest {
    @InjectMocks Handler handler;

    @Mock WebRequest webRequest;

    @BeforeEach
    void init(){
        MockitoAnnotations.initMocks(this);
    }

    private static final ResponseSchema RESPONSE_SCHEMA = new ResponseSchema(false, null);

    @Test
    void givenRuntimeExceptionWhenHandleBadCredentialsExceptionThenAssertResult(){
        // Given
        RuntimeException ex = new RuntimeException();
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleBadCredentialsException(ex);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenRuntimeExceptionWhenHandleAccessDeniedExceptionThenAssertResult(){
        // Given
        RuntimeException ex = new RuntimeException();
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleAccessDeniedException(ex);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenRuntimeExceptionWhenHandleServerExceptionThenAssertResult(){
        // Given
        ServerException ex = new ServerException(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{}, HttpStatus.BAD_REQUEST);
        List<String> response = new ArrayList<>();
        response.add(ex.getOmaErrorMessageType().getMessageId());
        response.add(ex.getOmaErrorMessageType().getText());
        RESPONSE_SCHEMA.setMessage(response);
        // When
        ResponseEntity<?> responseEntity = handler.handleServerException(ex);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenHttpRequestMethodNotSupportedExceptionWhenHandleHttpRequestMethodNotSupportedThenAssertResult(){
        // Given
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("");
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleHttpRequestMethodNotSupported(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenHttpMessageNotReadableExceptionWhenHandleHttpMessageNotReadableThenAssertResult(){
        // Given
        HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleHttpMessageNotReadable(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenHttpMediaTypeNotSupportedExceptionWhenHandleHttpMediaTypeNotSupportedThenAssertResult(){
        // Given
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("");
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleHttpMediaTypeNotSupported(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenExceptionWhenHandleGeneralExceptionThenAssertResult(){
        // Given
        Exception ex = new Exception("");
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleGeneralException(ex);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenNoHandlerFoundExceptionWhenHandleNoHandlerFoundExceptionThenAssertResult(){
        // Given
        NoHandlerFoundException ex = new NoHandlerFoundException("", "", new HttpHeaders());
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleNoHandlerFoundException(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenMissingPathVariableExceptionWhenHandleMissingPathVariableThenAssertResult(){
        // Given
        MissingPathVariableException ex = Mockito.mock(MissingPathVariableException.class);
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleMissingPathVariable(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenMissingPathVariableExceptionWhenHandleMissingServletRequestParameterThenAssertResult(){
        // Given
        MissingServletRequestParameterException ex = Mockito.mock(MissingServletRequestParameterException.class);
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleMissingServletRequestParameter(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
    @Test
    void givenMissingPathVariableExceptionWhenHandleTypeMismatchThenAssertResult(){
        // Given
        TypeMismatchException ex = Mockito.mock(TypeMismatchException.class);
        RESPONSE_SCHEMA.setMessage(new String[]{ex.getMessage()});
        // When
        ResponseEntity<?> responseEntity = handler.handleTypeMismatch(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(RESPONSE_SCHEMA);
    }
}
