package com.rauio.smartdangjian.annotation.validation.Validator;

import static com.rauio.smartdangjian.constants.ValidationConstants.PASSWORD_PATTERN;

import java.util.regex.Matcher;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.rauio.smartdangjian.annotation.validation.IsPassword;

public class IsPasswordValidator implements ConstraintValidator<IsPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }
}
