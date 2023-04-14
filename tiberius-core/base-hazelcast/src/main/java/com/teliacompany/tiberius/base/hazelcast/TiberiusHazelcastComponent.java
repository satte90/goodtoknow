package com.teliacompany.tiberius.base.hazelcast;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizePolicy;
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
public @interface TiberiusHazelcastComponent {
    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     *
     * @return the suggested component name, if any (or empty String otherwise)
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    String name();

    /**
     *  Set this to create a "model version" for the cache.
     *  If domain model has changed since last service deploy, already cached objects will not match the new service versions idea of how the objects are defined
     *  and it will fail. Setting a version of the cache will store new objects in a new cache map and old map will not be used. So use / change this only when needed
     *  as cache will effectively me wiped on new versions.
     */
    int version() default 1;

    String timeToLiveSeconds() default "600";

    String maxIdleSeconds() default "1200";

    EvictionPolicy evictionPolicy() default EvictionPolicy.LRU;

    MaxSizePolicy maxSizePolicy() default MaxSizePolicy.PER_NODE;

    String maxSize();

}
