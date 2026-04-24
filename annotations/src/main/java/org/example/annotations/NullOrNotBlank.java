package org.example.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = {})
public @interface NullOrNotBlank {
    String message() default "{field} must  be null or not blank";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
