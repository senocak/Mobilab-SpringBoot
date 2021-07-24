package com.github.senocak.controller;

import java.util.*;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import com.github.senocak.model.*;
import org.springframework.http.*;
import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import com.github.senocak.payload.RequestSchema;
import org.springframework.web.bind.annotation.*;
import com.github.senocak.payload.ResponseSchema;
import com.github.senocak.util.JsonSchemaValidator;
import com.github.senocak.util.OmaErrorMessageType;
import com.github.senocak.exception.ServerException;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(value = "HomeController", description = "Home Controller")
public class AuthController {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final AuthenticationManager authenticationManager;
    private final ResponseSchema response = new ResponseSchema(true, null);

    @PostMapping("/login")
    @ApiOperation(value = "Login Endpoint", response = ResponseSchema.class, tags = "auth")
    public ResponseEntity<?> login(@RequestBody RequestSchema.LoginRequest loginRequest) throws ServerException {
        jsonSchemaValidator.validateJsonSchema(loginRequest, RequestSchema.LoginRequest.class);
        if (loginRequest.getUsernameOrEmail().contains("@")) {
            if (!jsonSchemaValidator.isValidEmailAddress(loginRequest.getUsernameOrEmail())) {
                log.error("Email: {} is not valid.", loginRequest.getUsernameOrEmail());
                throw new ServerException(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{"Email is not valid"}, HttpStatus.BAD_REQUEST);
            }
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String generatedToken = "Bearer " + tokenProvider.generateToken(authentication);
        log.info("Token is generated. Token: {}", generatedToken);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", generatedToken);
        User user = userRepository.findByEmail(loginRequest.getUsernameOrEmail()).orElseThrow(() ->
            new ServerException(OmaErrorMessageType.NOT_FOUND, new String[]{"User:" + loginRequest.getUsernameOrEmail()}, HttpStatus.NOT_FOUND));
        responseMap.put("user", modelMapper.map(user, ResponseSchema.UserProfile.class));
        response.setMessage(responseMap);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    @ApiOperation(value = "Register Endpoint", response = ResponseSchema.class, tags = "auth")
    public ResponseEntity<?> register(@RequestBody RequestSchema.SignUpRequest signUpRequest) throws ServerException {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ServerException(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{"Username is already taken!"}, HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ServerException(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{"Email Address already in use!"}, HttpStatus.BAD_REQUEST);
        }
        jsonSchemaValidator.validateJsonSchema(signUpRequest, RequestSchema.SignUpRequest.class);
        User user = User.builder()
                .name(signUpRequest.getName())
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .build();
        User result = userRepository.save(user);
        log.info("User created. User: {}", result);
        response.setMessage(new String[]{"User registered successfully"});
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
