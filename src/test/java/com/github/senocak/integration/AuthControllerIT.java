package com.github.senocak.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.integration.annotations.SpringBootTestConfig;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.util.Random;

@SpringBootTestConfig
class AuthControllerIT {
    @Autowired WebApplicationContext webApplicationContext;
    @Autowired MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private static final String BASE_URL = "/api/v1/auth";

    @BeforeEach public void init() {
        objectMapper = new ObjectMapper();
    }
    @Test
    void givenSignUpRequestWithDuplicateUsernameWhenRegisterThenAssertResult() throws Exception {
        // Given
        RequestSchema.SignUpRequest signUpRequest = new RequestSchema.SignUpRequest();
        signUpRequest.setName(TestConstants.NAME);
        signUpRequest.setEmail(TestConstants.EMAIL + "1");
        signUpRequest.setPassword(TestConstants.PASS);
        signUpRequest.setUsername(TestConstants.NAME);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL+"/register")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("SVC0007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[1]").value("Schema failed."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[2]").value("Username is already taken!"));
    }
    @Test
    void givenLoginRequestWithInvalidEmailWhenLoginThenAssertResult() throws Exception {
        // Given
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        loginRequest.setUsernameOrEmail("@senocak.com");
        loginRequest.setPassword(TestConstants.PASS);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL+"/login")
            .content(objectMapper.writeValueAsString(loginRequest))
            .contentType(MediaType.APPLICATION_JSON);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("SVC0007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[1]").value("Schema failed."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[2]").value("Email is not valid"));
    }
    @Test
    void givenLoginRequestWithInvalidCredenatialsWhenLoginThenAssertResult() throws Exception {
        // Given
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        loginRequest.setUsernameOrEmail("email");
        loginRequest.setPassword("password");
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL+"/login")
            .content(objectMapper.writeValueAsString(loginRequest))
            .contentType(MediaType.APPLICATION_JSON);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("Bad credentials"));
    }
    @Test
    void givenLoginRequestWhenLoginThenAssertResult() throws Exception {
        // Given
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        loginRequest.setUsernameOrEmail(TestConstants.EMAIL);
        loginRequest.setPassword(TestConstants.PASS);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL+"/login")
            .content(objectMapper.writeValueAsString(loginRequest))
            .contentType(MediaType.APPLICATION_JSON);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.user.name").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.user.username").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.user.email").value(TestConstants.EMAIL))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.user.accounts").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.token").exists());
    }
    @Test
    void givenSignUpRequestWithDuplicateEmailWhenRegisterThenAssertResult() throws Exception {
        // Given
        RequestSchema.SignUpRequest signUpRequest = new RequestSchema.SignUpRequest();
        signUpRequest.setName(TestConstants.NAME);
        signUpRequest.setEmail(TestConstants.EMAIL);
        signUpRequest.setPassword(TestConstants.PASS);
        signUpRequest.setUsername(TestConstants.NAME + new Random().nextInt(10000)+10);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL+"/register")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("SVC0007"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[1]").value("Schema failed."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[2]").value("Email Address already in use!"));
    }
    @Test
    void givenSignUpRequestWhenRegisterThenAssertResult() throws Exception {
        // Given
        RequestSchema.SignUpRequest signUpRequest = new RequestSchema.SignUpRequest();
        signUpRequest.setName(TestConstants.NAME);
        signUpRequest.setEmail("anil@senocak.com");
        signUpRequest.setPassword(TestConstants.PASS);
        signUpRequest.setUsername(TestConstants.NAME + TestConstants.NAME);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL+"/register")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("User registered successfully"));
    }
}
