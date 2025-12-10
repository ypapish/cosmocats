package com.example.cosmocats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ApiKeyException extends RuntimeException {

  public ApiKeyException(String message) {
    super(message);
  }
}
