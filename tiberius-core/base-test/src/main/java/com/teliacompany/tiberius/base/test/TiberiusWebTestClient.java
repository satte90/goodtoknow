package com.teliacompany.tiberius.base.test;

import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TiberiusWebTestClient {
    String DEFAULT_FAKE_TCAD_ID = "xxx999";
    String DEFAULT_FAKE_TCWSS_ID = "1234";

    String tcadHeader() default DEFAULT_FAKE_TCAD_ID;

    String tcwssIdHeader() default DEFAULT_FAKE_TCWSS_ID;

    String defaultAcceptHeader() default MediaType.APPLICATION_JSON_VALUE;

    String defaultContentTypeHeader() default MediaType.APPLICATION_JSON_VALUE;

    int timeoutMinutes() default 10; //10 minutes should allow for some debugging :)

    String defaultHeaderKey() default "test";

    String defaultHeaderValue() default "true";
}
