package org.example.annotations_processors;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.annotations.ValidDifference;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class ValidDifferenceValidator implements ConstraintValidator<ValidDifference, Object> {
    private static final Logger logger= LogManager.getLogger(ValidDifferenceValidator.class);
    private String first;
    private String second;

    @Override
    public void initialize(ValidDifference constraintAnnotation) {
        first = constraintAnnotation.first();
        second = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        try{
            Field field = o.getClass().getDeclaredField(first);
            Field secondField = o.getClass().getDeclaredField(second);
            field.setAccessible(true);
            secondField.setAccessible(true);

            if (field == null || secondField == null) {
                return true;
            }

            BigDecimal lesserNum = toBigDecimal(field);
            BigDecimal greaterNum = toBigDecimal(secondField);

            if (lesserNum == null || greaterNum == null) {
                return true;
            }

            return lesserNum.compareTo(greaterNum) <= 0;

        }
        catch(Exception e){
            logger.error("ValidDifferenceValidator isValid: " + e.getMessage());
            return false;
        }

    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof Integer) return BigDecimal.valueOf((Integer) value);
        if (value instanceof Long) return BigDecimal.valueOf((Long) value);
        // Можно добавить поддержку других типов (Float, String), если нужно
        return null;
    }
}
