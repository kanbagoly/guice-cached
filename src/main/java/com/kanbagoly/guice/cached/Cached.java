package com.kanbagoly.guice.cached;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {

    /**
     * Define for how long the value in the cache is valid.
     */
    int duration();
    TimeUnit timeUnit();

    /**
     * Maximum size of the cache.
     *
     * Same or similar behaviour can be expected as the LRU
     * algorithm if the size of the cache reach this value.
     */
    int maxSize();

}
