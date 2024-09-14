package org.jsignal.prop;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Prop {
  /**
   * specifies that a prop needs to be set
   */
  boolean required() default false;

  /**
   * when at least one of a set of props needs to be assigned a value
   */
  String oneofKey() default "";

  /**
   * When the inner type of a Supplier prop is assignable to Supplier the setter needs to be named differently because
   * Java will not be able to do operator overloading. This suffix can be changed in order to avoid name clashes with
   * other properties.
   */
  String suffix() default "Const";

  /**
   * ensures that only one simple setter is generated, meant for disambiguating Supplier type props that should not be reactive
   */
  boolean noRx() default false;

  /**
   * do not generate a constant setter for this property
   */
  boolean noConst() default false;
}
