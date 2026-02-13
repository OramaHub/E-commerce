package com.orama.e_commerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

  @Override
  public boolean isValid(String cpf, ConstraintValidatorContext context) {
    if (cpf == null || cpf.isBlank()) {
      return true;
    }

    String digits = cpf.replaceAll("[^0-9]", "");

    if (digits.length() != 11) {
      return false;
    }

    if (digits.chars().distinct().count() == 1) {
      return false;
    }

    int sum = 0;
    for (int i = 0; i < 9; i++) {
      sum += Character.getNumericValue(digits.charAt(i)) * (10 - i);
    }
    int firstDigit = 11 - (sum % 11);
    if (firstDigit >= 10) {
      firstDigit = 0;
    }
    if (Character.getNumericValue(digits.charAt(9)) != firstDigit) {
      return false;
    }

    sum = 0;
    for (int i = 0; i < 10; i++) {
      sum += Character.getNumericValue(digits.charAt(i)) * (11 - i);
    }
    int secondDigit = 11 - (sum % 11);
    if (secondDigit >= 10) {
      secondDigit = 0;
    }
    return Character.getNumericValue(digits.charAt(10)) == secondDigit;
  }
}
