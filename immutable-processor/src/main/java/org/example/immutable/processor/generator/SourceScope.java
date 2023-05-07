package org.example.immutable.processor.generator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

/** Scope for generating source code from the object model. */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface SourceScope {}
