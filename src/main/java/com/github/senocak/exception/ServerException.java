package com.github.senocak.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import com.github.senocak.util.OmaErrorMessageType;

@Getter
@Setter
@AllArgsConstructor
public class ServerException extends Exception {
    private final OmaErrorMessageType omaErrorMessageType;
    private final String[] variables;
    private final HttpStatus statusCode;
}
