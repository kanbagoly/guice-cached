package com.kanbagoly.guice.cached;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.TimeUnit;

class Test1 {

    @Test
    void test() {
        TestClass cached = new TestClass();
        cached.method();
        cached.method();
        Assertions.assertEquals(1, cached.invocationCounter);
    }

    static class TestClass {

        private int invocationCounter = 0;

        @Cached(duration = 1, timeUnit = TimeUnit.DAYS, maxSize = 10)
        int method() {
            invocationCounter += 1;
            return 1;
        }

        int numberOfInvocations() {
            return invocationCounter;
        }

    }

}
