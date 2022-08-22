package com.runsidekick.audit.logger.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yasin.kalafat
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.PARAMETER, ElementType.FIELD})
public @interface AuditField {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
