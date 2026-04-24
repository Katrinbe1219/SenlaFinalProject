package org.example.annotations_processors;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.annotations.MutuallyExclusive;

import java.lang.reflect.Field;

public class MutuallyExclusiveValidator implements ConstraintValidator<MutuallyExclusive, Object> {
    private static  final Logger logger = LogManager.getLogger(MutuallyExclusiveValidator.class);
    private String fields1;
    private String fields2;

    @Override
    public void initialize(MutuallyExclusive constraintAnnotation) {
        this.fields1 = constraintAnnotation.fields1();
        this.fields2 = constraintAnnotation.fields2();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext ctx) {
        try{
            Field f1 = obj.getClass().getDeclaredField(fields1);
            Field f2 = obj.getClass().getDeclaredField(fields2);
            f1.setAccessible(true);
            f2.setAccessible(true);
            boolean bothPresent = f1.get(obj) != null && f2.get(obj) != null;
            if (bothPresent){
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(
                        "Either " + fields1 + " or " + fields2 +  " must be given"
                ).addConstraintViolation();
            }
            return !bothPresent;

        }
        catch (Exception e){
            logger.error("MutuallyExclusiveValidator isValid: " + e.getMessage());
            return false;
        }
    }
}
