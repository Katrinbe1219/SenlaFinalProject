package org.example.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
// чтобы не было цикличности не указываем класс для валидации из другого модуля
@Constraint(validatedBy = {})
@Target(ElementType.TYPE)
// позволяет использовать на один и тот же класс - элемент
// поэтому и нужен List для того чтобы компилятор создал лист с аннотациями для элемента
@Repeatable(MutuallyExclusive.List.class)
public @interface MutuallyExclusive {
    String message() default "Either {fields1} or {fields2}";
    Class<?>[] groups() default  {};
    // чтобы передать метаданные ошибки тому, кто обрабатывает
    Class<? extends Payload>[] payload() default {};
    String fields1();
    String fields2();


            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE)
            @interface  List{
                MutuallyExclusive[] value();
            }
}
