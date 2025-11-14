package com.rauio.ZhihuiDangjiang.annotation.validation.Validator;

import com.rauio.ZhihuiDangjiang.annotation.validation.IsPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;

import static com.rauio.ZhihuiDangjiang.constants.ValidationConstants.PASSWORD_PATTERN;

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
