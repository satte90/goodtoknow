package com.teliacompany.tiberius.base.businessrules;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BusinessRuleComponent {
    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     *
     * @return the suggested component name, if any (or empty String otherwise)
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    String summary();

    String description();

    String appliedWhenInfo() default "Always";

    /**
     * Override this if your rule is dependent on one or more other rules.
     */
    Class<? extends BusinessRule>[] dependsOn() default {};

    /**
     * Override this if the rule should only be used for some requests, overrides dontApplyForRequests
     */
    String[] applyOnlyForRequests() default {};

    /**
     * Override this if the rule should not be used for some requests
     */
    String[] dontApplyForRequests() default {};
}
