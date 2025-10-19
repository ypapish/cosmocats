package com.example.cosmocats.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CosmicWordValidator implements ConstraintValidator<CosmicWordCheck, String> {

  private static final List<String> COSMIC_TERMS =
      Arrays.asList(
          "galaxy",
          "star",
          "cosmic",
          "space",
          "nebula",
          "comet",
          "orbit",
          "lunar",
          "solar",
          "asteroid",
          "planet",
          "meteor",
          "universe",
          "cosmos",
          "astro",
          "quantum",
          "interstellar",
          "black hole",
          "supernova",
          "constellation",
          "milky way");

  private int minWords;

  @Override
  public void initialize(CosmicWordCheck constraintAnnotation) {
    this.minWords = constraintAnnotation.minWords();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return true;
    }

    String lowerCaseValue = value.toLowerCase();
    long cosmicWordCount =
        COSMIC_TERMS.stream().filter(term -> containsWord(lowerCaseValue, term)).count();

    return cosmicWordCount >= minWords;
  }

  private boolean containsWord(String text, String word) {
    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word.toLowerCase()) + "\\b");
    return pattern.matcher(text).find();
  }
}
