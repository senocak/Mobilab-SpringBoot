package com.github.senocak.controller;

import com.github.senocak.service.UserService;
import com.github.senocak.util.JsonSchemaValidator;
import com.github.senocak.util.TestConstants;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.User;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.payload.ResponseSchema;
import com.github.senocak.security.JwtTokenProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashMap;
import java.util.Map;

class AuthControllerTest {
    @InjectMocks AuthController authController;

    @Mock UserService userService;
    @Mock ModelMapper modelMapper;
    @Mock JsonSchemaValidator jsonSchemaValidator;
    @Mock JwtTokenProvider tokenProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock Authentication authentication;
    private final ResponseSchema response = new ResponseSchema(true, null);

    @BeforeEach
    void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void givenLoginRequestWithNotValidEmailWhenLoginThenAssertResult() {
        // Given
        String email = "not@Valid";
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        loginRequest.setUsernameOrEmail(email);
        loginRequest.setPassword(TestConstants.PASS);
        Mockito.doReturn(false).when(jsonSchemaValidator).isValidEmailAddress(email);
        // When
        Executable result = () -> authController.login(loginRequest);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }

    @Test
    void givenLoginRequestWithExceptionWhenLoginThenAssertResult() throws ServerException {
        // Given
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        loginRequest.setUsernameOrEmail(TestConstants.EMAIL);
        loginRequest.setPassword(TestConstants.PASS);
        Mockito.doReturn(true).when(jsonSchemaValidator).isValidEmailAddress(TestConstants.EMAIL);

        Mockito.doReturn(authentication).when(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(
            loginRequest.getUsernameOrEmail(),
            loginRequest.getPassword()
        ));
        Mockito.doReturn(TestConstants.TOKEN).when(tokenProvider).generateToken(authentication);
        Mockito.doThrow(ServerException.class).when(userService).findByEmail(TestConstants.EMAIL);
        // When
        Executable result = () -> authController.login(loginRequest);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }

    @Test
    void givenLoginRequestWhenLoginThenAssertResult() throws ServerException {
        // Given
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        loginRequest.setUsernameOrEmail(TestConstants.EMAIL);
        loginRequest.setPassword(TestConstants.PASS);
        Mockito.doReturn(true).when(jsonSchemaValidator).isValidEmailAddress(TestConstants.EMAIL);

        Mockito.doReturn(authentication).when(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(
            loginRequest.getUsernameOrEmail(),
            loginRequest.getPassword()
        ));
        Mockito.doReturn(TestConstants.TOKEN).when(tokenProvider).generateToken(authentication);
        Mockito.doReturn(TestConstants.USER_1).when(userService).findByEmail(TestConstants.EMAIL);
        Mockito.doReturn(TestConstants.USER_PROFILE).when(modelMapper).map(TestConstants.USER_1, ResponseSchema.UserProfile.class);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", "Bearer " + TestConstants.TOKEN);
        responseMap.put("user", TestConstants.USER_PROFILE);
        response.setMessage(responseMap);
        // When
        ResponseEntity<?> responseEntity = authController.login(loginRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }

    @Test
    void givenSignUpRequestWithExistsUsernameWhenRegisterThenAssertResult() {
        // Given
        RequestSchema.SignUpRequest signUpRequest = new RequestSchema.SignUpRequest();
        signUpRequest.setUsername(TestConstants.NAME);
        signUpRequest.setPassword(TestConstants.PASS);
        signUpRequest.setEmail(TestConstants.EMAIL);
        signUpRequest.setName(TestConstants.NAME);
        Mockito.doReturn(true).when(userService).existsByUsername(TestConstants.NAME);
        // When
        Executable result = () -> authController.register(signUpRequest);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenSignUpRequestWithExistsEmailWhenRegisterThenAssertResult() {
        // Given
        RequestSchema.SignUpRequest signUpRequest = new RequestSchema.SignUpRequest();
        signUpRequest.setUsername(TestConstants.NAME);
        signUpRequest.setPassword(TestConstants.PASS);
        signUpRequest.setEmail(TestConstants.EMAIL);
        signUpRequest.setName(TestConstants.NAME);
        Mockito.doReturn(false).when(userService).existsByUsername(TestConstants.NAME);
        Mockito.doReturn(true).when(userService).existsByEmail(TestConstants.EMAIL);
        // When
        Executable result = () -> authController.register(signUpRequest);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenSignUpRequestWhenRegisterThenAssertResult() throws ServerException {
        // Given
        RequestSchema.SignUpRequest signUpRequest = new RequestSchema.SignUpRequest();
        signUpRequest.setUsername(TestConstants.NAME);
        signUpRequest.setPassword(TestConstants.PASS);
        signUpRequest.setEmail(TestConstants.EMAIL);
        signUpRequest.setName(TestConstants.NAME);
        Mockito.doReturn(false).when(userService).existsByUsername(TestConstants.NAME);
        Mockito.doReturn(false).when(userService).existsByEmail(TestConstants.EMAIL);
        Mockito.doReturn(TestConstants.PASS).when(passwordEncoder).encode(TestConstants.PASS);
        Mockito.doReturn(TestConstants.USER_1).when(userService).save(Mockito.any(User.class));
        response.setMessage(new String[]{"User registered successfully"});
        // When
        ResponseEntity<?> responseEntity = authController.register(signUpRequest);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }
}
