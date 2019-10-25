package com.kanbagoly.guice.cached;


import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

import com.google.inject.AbstractModule;

/**
 * <p>
 * This module provide a solution to create cached methods.</br>
 * This can be a good idea if the method execute something expensive,
 * like load data from the database or create a big graph...
 * </p>
 *
 * To achieve this simply install this module in your Guice module and mark
 * the chosen method with the {@link Cached} annotation like this:
 * <pre>
 * {@literal @}Cached(duration = 1, timeUnit = TimeUnit.HOURS, maxSize = 100)
 * Bean selectBeanFromDatabase(long id) {
 *     ...
 * }</pre>
 *
 * @see com.kanbagoly.guice.cached.Cached
 */
public class MethodCacheModule extends AbstractModule {

    @Override
    protected void configure() {
        CachedMethodInterceptor interceptor = new CachedMethodInterceptor();
        requestInjection(interceptor);
        bindInterceptor(any(), annotatedWith(Cached.class), interceptor);
    }

}