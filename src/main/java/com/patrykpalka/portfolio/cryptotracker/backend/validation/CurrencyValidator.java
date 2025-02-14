package com.patrykpalka.portfolio.cryptotracker.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private final CurrencyValidationService validationService;

    public CurrencyValidator(CurrencyValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext constraintValidatorContext) {
        return validationService.isValidCurrency(currency);
    }
}
