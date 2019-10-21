package com.kanbagoly.guice.cached;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

class CachedMethodInterceptor implements MethodInterceptor {

    private final Map<Method, Cache<ParameterContainer, Object>> caches = new ConcurrentHashMap<>();

    @Override
    public Object invoke(final MethodInvocation invocation) throws ExecutionException {
        final Method method = invocation.getMethod();

        /* Create the cache if the method will be executed at first time.
         * Double checking lock for ensure fast thread-safe behaviour.
         */
        if (!caches.containsKey(method)) {
            synchronized(this) {
                if (!caches.containsKey(method)) {
                    Cached settings = method.getAnnotation(Cached.class);
                    Cache<ParameterContainer, Object> cache = CacheBuilder.newBuilder()
                            .expireAfterWrite(settings.duration(), settings.timeUnit())
                            .maximumSize(settings.maxSize())
                            .build();
                    caches.put(method, cache);
                }
            }
        }

        Cache<ParameterContainer, Object> cache = caches.get(method);
        final Object[] parameters = invocation.getArguments();
        return cache.get(new ParameterContainer(parameters), () -> {
            try {
                return invocation.proceed();
            } catch (Throwable e) {
                throw new ExecutionException(e);
            }
        });
    }

}

