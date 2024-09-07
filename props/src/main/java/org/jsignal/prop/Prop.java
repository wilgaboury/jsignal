package org.jsignal.prop;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Prop {
  boolean required() default false;
  String oneofKey() default "";
  String suffix() default "Const";
  Converter[] converter() default {};

  @interface Converter {
    // TODO: implement
  }
}
