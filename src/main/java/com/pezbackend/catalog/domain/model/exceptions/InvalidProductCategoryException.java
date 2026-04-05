package com.pezbackend.catalog.domain.model.exceptions;

import com.pezbackend.shared.domain.model.exceptions.BadRequestException;

public class InvalidProductCategoryException extends BadRequestException {
  public InvalidProductCategoryException() {
    super("Product category cannot be null");
  }
}