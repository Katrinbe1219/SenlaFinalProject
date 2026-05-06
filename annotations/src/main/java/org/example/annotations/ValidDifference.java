package org.example.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ValidDifference.List.class)
@Constraint(validatedBy = {})
public @interface ValidDifference {

    String message() default "{first} must be <= {second}";
    String first();
    String second();

    Class<? extends Payload>[] payload()  default {};
    Class<?>[] groups() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List{
        ValidDifference[] value();
    }
}
