package com.example.cosmocats.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
  public CategoryAlreadyExistsException(String categoryName) {
    super("Category already exists with name: " + categoryName);
  }
}
