package com.rauio.smartdangjian.annotation.validation.Validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.rauio.smartdangjian.annotation.validation.IsPhone;
import com.rauio.smartdangjian.constants.ValidationConstants;

public class IsPhoneValidator implements ConstraintValidator<IsPhone, String> {

    private boolean required;

    @Override
    public void initialize(IsPhone annotation) {
        this.required = annotation.required();
    }

    private boolean isRequired() {
        return required;
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null) {
            return !isRequired();
        }
        return ValidationConstants.PHONE_PATTERN.matcher(phone).matches();
    }
}
