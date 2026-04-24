package org.example.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// Constraint - говорит, что данная аннотация является ограничителем, без нее это просто аннотация
@Constraint(validatedBy = {})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(MutuallyExclusiveExtended.List.class)
public @interface MutuallyExclusiveExtended {
    String message() default "Either {first} or {second}  + {third}";
    Class<? extends Payload>[] payload() default {};
    Class<?>[] groups() default {};
    String first();
    String second();
    String third();
    boolean both() default false;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List{
        MutuallyExclusiveExtended[] value();
    }
}
