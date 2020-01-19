package com.arise.cargo.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UXField {
   String value() default "";
   InputType type() default InputType.TEXT;
   boolean required() default false;
   boolean readonly() default false;
   String label() default "";
   String description() default "";
}
