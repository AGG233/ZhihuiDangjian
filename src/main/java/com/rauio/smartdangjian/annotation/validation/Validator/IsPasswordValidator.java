package com.rauio.smartdangjian.annotation.validation.Validator;

import com.rauio.smartdangjian.annotation.validation.IsPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;

import static com.rauio.smartdangjian.constants.ValidationConstants.PASSWORD_PATTERN;

public class IsPasswordValidator implements ConstraintValidator<IsPassword, String>{

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if (password == null){
            return false;
        }
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }
}
