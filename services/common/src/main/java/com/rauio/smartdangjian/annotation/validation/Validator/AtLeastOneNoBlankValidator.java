package com.rauio.smartdangjian.annotation.validation.Validator;

import com.rauio.smartdangjian.annotation.validation.AtLeastOneNoBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

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
            for (String fieldName : fieldNames) {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object fieldValue = field.get(object);

                if (fieldValue instanceof String stringValue && StringUtils.hasText(stringValue)) {
                    return true;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("[AtLeastOneNoBlankError] " + e.getMessage(), e);
        }
        return false;
    }
}
