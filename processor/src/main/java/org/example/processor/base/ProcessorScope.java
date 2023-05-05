package org.example.processor.base;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.inject.Scope;

/** Scope for objects whose lifecycle begins with {@link Processor#init(ProcessingEnvironment)}. */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProcessorScope {}
