package com.rauio.smartdangjian.annotation.validation.Validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.rauio.smartdangjian.annotation.validation.IsIdCard;

import cn.hutool.core.util.IdcardUtil;

public class IsIdCardValidator implements ConstraintValidator<IsIdCard, String> {

    @Override
    public boolean isValid(String idCard, ConstraintValidatorContext context) {
        if (idCard == null) {
            return false;
        }
        return IdcardUtil.isValidCard(idCard);
    }
}
