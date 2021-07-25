package com.github.senocak.util;

import com.networknt.schema.ValidatorTypeCode;

public enum ErrorMessageType {
  BASIC_INVALID_INPUT("SVC0001", "Invalid input value for message part %1"),
  GENERIC_SERVICE_ERROR("SVC0002", "The following service error occurred: %1. Error code is %2"),
  DETAILED_INVALID_INPUT("SVC0003", "Invalid input value for %1 %2: %3"),
  EXTRA_INPUT_NOT_ALLOWED("SVC0004", "Input %1 %2 not permitted in request"),
  MANDATORY_INPUT_MISSING("SVC0005", "Mandatory input %1 %2 is missing from request"),
  JSON_SCHEMA_VALIDATOR("SVC0007", "Schema failed."),
  NOT_FOUND("SVC0008", "Entry is not found");
  // https://www.openmobilealliance.org/wp/OMNA/RESTful_Network_APIs/service-and-policy-exception-codes-registry-for-oma-restful-network-APIs.html

  private final String messageId;
  private final String text;

  ErrorMessageType(final String messageId, final String text) {
    this.messageId = messageId;
    this.text = text;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getText() {
    return text;
  }

  public static ErrorMessageType getOmaErrorFromValidationError(ValidatorTypeCode failureCode) {
    switch (failureCode) {
      case MIN_LENGTH:
      case MAX_LENGTH:
      case MINIMUM:
      case MAXIMUM:
      case MAX_ITEMS:
      case MIN_ITEMS:
      case TYPE:
      case PATTERN:
        return DETAILED_INVALID_INPUT;
      case ENUM:
        return BASIC_INVALID_INPUT;
      case REQUIRED:
        return MANDATORY_INPUT_MISSING;
      case ADDITIONAL_PROPERTIES:
        return EXTRA_INPUT_NOT_ALLOWED;
      default:
        return GENERIC_SERVICE_ERROR;
    }
  }
}
