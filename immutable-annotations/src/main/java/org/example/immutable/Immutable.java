package org.example.immutable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an interface defining an immutable type, instructing the processor to generate an implementation.
 *
 * <p>The annotated type must be an interface, and any instance methods (except default methods)
 * must not have parameters or type parameters.</p>
 *
 * <p>The generated implementation for interface {@code [Type]} is a class named {@code Immutable[Type]},
 * and it will be in the same package as the interface. The generated constructor will have package visibility,
 * so the interface must provide a static factory method that invokes the constructor.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Immutable {}
