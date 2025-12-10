package com.example.cosmocats.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CosmicWordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CosmicWordCheck {
  String message() default "Product name must contain at least one cosmic term";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int minWords() default 1;
}
