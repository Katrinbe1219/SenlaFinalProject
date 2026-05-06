package org.example.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ValidDateRange.List.class)
@Constraint(validatedBy = {})
public @interface ValidDateRange {

    String message() default "{first} must be <= {second}";
    String first();
    String second();

    Class<? extends Payload>[] payload()  default {};
    Class<?>[] groups() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface  List{
        ValidDateRange[] value();
    }
}
