package org.jsignal.prop;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Prop {
  boolean required() default false; // TODO: implement
  String oneofKey() default ""; // TODO: implement

  String suffix() default "Const";

  boolean noConst() default false;

  // TODO: implement
  Converter[] converter() default {};
  @interface Converter {}
}
