package com.github.senocak.util;

import java.util.Set;
import java.util.Objects;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import com.networknt.schema.JsonSchema;
import org.springframework.http.HttpStatus;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.exception.ServerException;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

@Service
@Slf4j
public class JsonSchemaValidator {
    private static final String EMPTY_BODY = "Request must have a body";
    private static final String UNABLE_VALIDATE = "Unable to validate json message due to an exception";
    private static final String ATTRIBUTE = "attribute";
    private static final char DOT = '.';
    private static final char COLON = ':';

    private final ObjectMapper objectMapper;
    private final JsonSchemaGenerator jsonSchemaGenerator;

    public JsonSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);
    }

    public void validateJsonSchema(Object jsonBodyToValidate, Class<?> jsonSchemaClass) throws ServerException {
        if (Objects.isNull(jsonBodyToValidate)) {
            log.error(EMPTY_BODY);
            throw new ServerException(ErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{EMPTY_BODY}, HttpStatus.BAD_REQUEST);
        }

        final Set<ValidationMessage> validationResult;
        try {
            validationResult = validateJsonMsgFromObject(jsonBodyToValidate, jsonSchemaClass);
        } catch (Exception ex) {
            log.error(UNABLE_VALIDATE);
            throw new ServerException(ErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{UNABLE_VALIDATE}, HttpStatus.BAD_REQUEST);
        }
        checkValidationResult(validationResult);
    }

    private Set<ValidationMessage> validateJsonMsgFromObject(final Object objectToValidate,
                                                             final Class<?> jsonSchemaClass) throws IOException {
        final JsonNode jsonNodeForSchema = jsonSchemaGenerator.generateJsonSchema(jsonSchemaClass);
        JsonSchema schema = JsonSchemaFactory.getInstance().getSchema(jsonNodeForSchema);
        JsonNode jsonToValidate;
        if (objectToValidate instanceof String) {
            log.info("objectToValidate is instanceof String");
            jsonToValidate = objectMapper.readTree((String) objectToValidate);
        } else {
            log.info("objectToValidate is not instanceof String");
            jsonToValidate = objectMapper.valueToTree(objectToValidate);
        }
        return schema.validate(jsonToValidate);
    }

    private void checkValidationResult(Set<ValidationMessage> validationResults) throws ServerException {
        boolean result = CollectionUtils.isEmpty(validationResults);
        log.info("ValidationResult: {}", result);
        if (!result) {
            log.info("As a rule, we'll only notify the client about the first validation error");
            final ValidationMessage errorMessage = validationResults.iterator().next();
            final ValidatorTypeCode failureCode = ValidatorTypeCode.fromValue(errorMessage.getType());
            String failedField;

            // Normally we can get the failedField pretty easily, except if it's a REQUIRED orADDITIONAL_PROPERTIES type failure. We have to process stuff a bit differently if it is one of these two cases.
            switch (failureCode) {
                case REQUIRED:
                case ADDITIONAL_PROPERTIES:
                    String message = errorMessage.getMessage();
                    failedField = message.substring(message.lastIndexOf(DOT) + 1, message.lastIndexOf(COLON));
                    break;
                default:
                    failedField = errorMessage.getPath().substring(errorMessage.getPath().lastIndexOf(DOT) + 1);
                    break;
            }
            ErrorMessageType errorMessageType = ErrorMessageType.getOmaErrorFromValidationError(failureCode);
            String[] variables = generateRequestErrorVariables(failureCode, failedField, errorMessage);
            log.info("Failures: {}", String.join(",", variables));
            throw new ServerException(errorMessageType, variables, HttpStatus.BAD_REQUEST);
        }
    }
    private String[] generateRequestErrorVariables(ValidatorTypeCode failureCode, String failedField, ValidationMessage errorMessage) {
        String validationArgument;
        String validationArgument2;
        try {
            validationArgument = errorMessage.getArguments()[0];
        } catch (Exception ex) {
            validationArgument = "";
        }
        try {
            validationArgument2 = errorMessage.getArguments()[1];
        } catch (Exception ex) {
            validationArgument2 = "";
        }
        switch (failureCode) {
            case MIN_LENGTH:
                return new String[]{ATTRIBUTE, failedField, String.format("field too short; minimum length is %s characters", validationArgument)};
            case MAX_LENGTH:
                return new String[]{ATTRIBUTE, failedField, String.format("field too long; maximum length is %s characters", validationArgument)};
            case MINIMUM:
                return new String[]{ATTRIBUTE, failedField, String.format("value is lower than the system limit of %s", validationArgument)};
            case MAXIMUM:
                return new String[]{ATTRIBUTE, failedField, String.format("value is greater than the system limit of %s", validationArgument)};
            case MAX_ITEMS:
                return new String[]{ATTRIBUTE, failedField, String.format("number of elements exceeds the system limit of %s", validationArgument)};
            case MIN_ITEMS:
                return new String[]{ATTRIBUTE, failedField, String.format("number of elements is below the system limit of %s", validationArgument)};
            case TYPE:
                return new String[]{ATTRIBUTE, failedField, String.format("found datatype %s, but required datatype is %s", validationArgument, validationArgument2)};
            case REQUIRED:
            case ADDITIONAL_PROPERTIES:
                return new String[]{ATTRIBUTE, failedField};
            case PATTERN:
                return new String[]{ATTRIBUTE, failedField, String.format("field does not match the pattern (%s)", validationArgument)};
            case ENUM:
                return new String[]{failedField};
            default:
                return new String[]{"unknown validation error", String.valueOf(HttpStatus.BAD_REQUEST.value())};
        }
    }
    public boolean isValidEmailAddress(String email) {
        log.info("Email validation is started.");
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
