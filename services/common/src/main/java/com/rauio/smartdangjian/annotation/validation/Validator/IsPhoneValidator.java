package com.rauio.smartdangjian.annotation.validation.Validator;

import com.rauio.smartdangjian.annotation.validation.IsPhone;
import com.rauio.smartdangjian.constants.ValidationConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsPhoneValidator implements ConstraintValidator<IsPhone, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null) {
            return false;
        }
        return ValidationConstants.PHONE_PATTERN.matcher(phone).matches();
    }
}
