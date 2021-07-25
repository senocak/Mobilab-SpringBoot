package com.github.senocak.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.exception.ServerException;
import com.github.senocak.payload.RequestSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockitoAnnotations;

class JsonSchemaValidatorTest {
    JsonSchemaValidator jsonSchemaValidator;
    ObjectMapper objectMapper;

    @BeforeEach
    void init(){
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
        jsonSchemaValidator = new JsonSchemaValidator(objectMapper);
    }
    @Test
    void givenJsonObjectAndSchemaClassWithObjectIsNullWhenValidateJsonSchemaThenAssertResult(){
        // When
        Executable result = () -> jsonSchemaValidator.validateJsonSchema(null, RequestSchema.LoginRequest.class);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenJsonObjectAndSchemaClassWithSchemaIsNullWhenValidateJsonSchemaThenAssertResult(){
        // When
        Executable result = () -> jsonSchemaValidator.validateJsonSchema(new RequestSchema.LoginRequest(), null);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenJsonObjectWhenValidateJsonSchemaThenAssertResult(){
        // Given
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        // When
        Executable result = () -> jsonSchemaValidator.validateJsonSchema(loginRequest, RequestSchema.LoginRequest.class);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
}
