package com.rauio.ZhihuiDangjian.annotation.validation.Validator;

import com.rauio.ZhihuiDangjian.constants.ValidationConstants;
import com.rauio.ZhihuiDangjian.annotation.validation.IsPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsPhoneValidator implements ConstraintValidator<IsPhone, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext constraintValidatorContext) {
        if (phone == null){
            return false;
        }
       return ValidationConstants.PHONE_PATTERN.matcher(phone).matches();
    }
}
