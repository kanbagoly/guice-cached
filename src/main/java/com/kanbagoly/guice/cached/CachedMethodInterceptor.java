package com.kanbagoly.guice.cached;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

class CachedMethodInterceptor implements MethodInterceptor {

    private final Map<Method, Cache<ParameterContainer, Object>> caches = new ConcurrentHashMap<>();

    @Override
    public Object invoke(final MethodInvocation invocation) throws ExecutionException {
        Cache<ParameterContainer, Object> cache = caches.computeIfAbsent(invocation.getMethod(), method -> {
            Cached settings = method.getAnnotation(Cached.class);
            return CacheBuilder.newBuilder()
                    .expireAfterWrite(settings.duration(), settings.timeUnit())
                    .maximumSize(settings.maxSize())
                    .build();
        });
        Object[] parameters = invocation.getArguments();
        return cache.get(new ParameterContainer(parameters), () -> {
            try {
                return invocation.proceed();
            } catch (Throwable e) {
                throw new ExecutionException(e);
            }
        });
    }

}

