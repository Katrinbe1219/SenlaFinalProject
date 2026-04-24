package org.example.annotations_processors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.annotations.MutuallyExclusiveExtended;

import java.lang.reflect.Field;

public class MutuallyExtendedValidator implements ConstraintValidator<MutuallyExclusiveExtended,Object> {
    private static final Logger logger = LogManager.getLogger(MutuallyExtendedValidator.class);
    private String first;
    private String second;
    private String third;
    private boolean both;

    @Override
    public void initialize(MutuallyExclusiveExtended constraintAnnotation) {
        first = constraintAnnotation.first();
        second = constraintAnnotation.second();
        third = constraintAnnotation.third();
        both = constraintAnnotation.both();
    }
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext ctx) {
        try {
            Field f1 = obj.getClass().getDeclaredField(first);
            Field f2 = obj.getClass().getDeclaredField(second);
            Field f3 = obj.getClass().getDeclaredField(third);

            f1.setAccessible(true);
            f2.setAccessible(true);
            f3.setAccessible(true);


            boolean allSet = f1.get(obj) != null && (f2.get(obj) != null || f3.get(obj) != null);
            boolean partial = (f2.get(obj) != null && f3.get(obj) == null) || (f2.get(obj) == null && f3.get(obj) != null);

            if (allSet){
                sendMessage("Either " + first + " or " + second + " + " + third,ctx);
                return false;
            }

            if (both &&partial){
                sendMessage("Both must be given: " + second + " " + third, ctx);
                return false;
            }
            return true;

        } catch (Exception e) {
            logger.error("MutuallyExtendedValidator  isValid:  " + e.getMessage());
            return false;
        }
    }

    private void sendMessage(String msg, ConstraintValidatorContext ctx){
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
    }
}
