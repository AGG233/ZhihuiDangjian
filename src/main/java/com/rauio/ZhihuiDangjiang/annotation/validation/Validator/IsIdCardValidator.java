package com.rauio.ZhihuiDangjiang.annotation.validation.Validator;

import cn.hutool.core.util.IdcardUtil;
import com.rauio.ZhihuiDangjiang.annotation.validation.IsIdCard;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsIdCardValidator implements ConstraintValidator<IsIdCard,String> {
    @Override
    public boolean isValid(String idCard, ConstraintValidatorContext context) {
        if (idCard == null){
            return false;
        }
        return IdcardUtil.isValidCard(idCard);
    }
}
