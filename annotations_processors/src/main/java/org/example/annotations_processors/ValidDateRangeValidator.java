package org.example.annotations_processors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.annotations.ValidDateRange;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    private static final Logger logger = LogManager.getLogger(ValidDateRangeValidator.class);

    private  String first;
    private  String second;
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.first = constraintAnnotation.first();
        this.second = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        try{
            Field field1 = o.getClass().getDeclaredField(first);
            Field field2 = o.getClass().getDeclaredField(second);
            field1.setAccessible(true);
            field2.setAccessible(true);

            LocalDate firstDate = (LocalDate) field1.get(o);
            LocalDate secondDate = (LocalDate) field2.get(o);

            if (firstDate == null || secondDate == null) {
                return true;
            }

            return !secondDate.isBefore(firstDate);
        }
        catch (Exception e){
            logger.error("ValidDateRangeValidator isValid " + e.getMessage());
            return false;
        }
    }
}
