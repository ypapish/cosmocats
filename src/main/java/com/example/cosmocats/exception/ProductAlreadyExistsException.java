package com.example.cosmocats.exception;

public class ProductAlreadyExistsException extends RuntimeException {
  public ProductAlreadyExistsException(String productName) {
    super("Product already exists with name: " + productName);
  }
}
