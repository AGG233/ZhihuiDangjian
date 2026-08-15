package com.rauio.smartdangjian.annotation.validation.Validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.rauio.smartdangjian.annotation.validation.IsIdCard;

import cn.hutool.core.util.IdcardUtil;

public class IsIdCardValidator implements ConstraintValidator<IsIdCard, String> {

    private boolean required;

    @Override
    public void initialize(IsIdCard annotation) {
        this.required = annotation.required();
    }

    private boolean isRequired() {
        return required;
    }

    @Override
    public boolean isValid(String idCard, ConstraintValidatorContext context) {
        if (idCard == null) {
            return !isRequired();
        }
        return IdcardUtil.isValidCard(idCard);
    }
}
