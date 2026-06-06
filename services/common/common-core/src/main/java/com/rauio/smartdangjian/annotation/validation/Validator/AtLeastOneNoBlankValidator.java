package com.rauio.smartdangjian.annotation.validation.Validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import com.rauio.smartdangjian.annotation.validation.AtLeastOneNoBlank;

public class AtLeastOneNoBlankValidator implements ConstraintValidator<AtLeastOneNoBlank, Object> {

    private String[] fieldNames;

    @Override
    public void initialize(AtLeastOneNoBlank constraintAnnotation) {
        this.fieldNames = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return true;
        }
        try {
            BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);
            for (String fieldName : fieldNames) {
                Object fieldValue = beanWrapper.getPropertyValue(fieldName);

                if (fieldValue instanceof String stringValue && StringUtils.hasText(stringValue)) {
                    return true;
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("[AtLeastOneNoBlankError] " + e.getMessage(), e);
        }
        return false;
    }
}
