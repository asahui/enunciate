package com.webcohesion.enunciate.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to apply a set of styles to an API component.
 *
 * @author Ryan Heaton
 */
@Target (
  { ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD ,ElementType.TYPE, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE }
)
@Retention (
  RetentionPolicy.RUNTIME
)
public @interface Styles {

  /**
   * The applicable styles.
   *
   * @return The applicable styles.
   */
  Style[] value();
}
