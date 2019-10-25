package com.kanbagoly.guice.cached;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CachedMethodTest {

    private static final int CACHE_TIME_TO_LIVE_IN_MS = 200;

    private static final int CACHE_SIZE = 5;

    private static final List<String> STRINGS_WITH_SAME_HASCODES = Arrays.asList(
            "Microcomputers: the unredeemed lollipop...",
            "Incentively, my dear, I don't tessellate a derangement.");

    private static Injector injector;
    private Expensive expensive;

    @BeforeAll
    static void createInjector() {
        injector = Guice.createInjector(new CachedMethodModule());
    }

    @BeforeEach
    void setUp() {
        expensive = injector.getInstance(Expensive.class);
    }
    public static class Expensive {

        private int counter = 0;

        @Cached(duration = CACHE_TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer size(String string) {
            ++counter;
            return string.length();
        }

        @Cached(duration = CACHE_TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer sumOfSizes(String... strings) {
            ++counter;
            int sum = 0;
            for (String string: strings) {
                sum += string.length();
            }
            return sum;
        }

        @Cached(duration = CACHE_TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer meaningOfLife() {
            ++counter;
            return 42;
        }

        @Cached(duration = CACHE_TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer dangerous(String strings) {
            throw new NullPointerException();
        }

        public int getCounter() {
            return counter;
        }
    }

    @Test
    void methodWithoutParameterShouldBeAbleToUseCahce() {
        expensive.meaningOfLife();

        expensive.meaningOfLife();

        assertEquals(1, expensive.getCounter());
    }

    @Test
    void methodShouldBeExecutedOnlyOnceIfCalledWithTheSameParameters() {
        expensive.size("Hi");

        expensive.size("Hi");

        assertEquals(1, expensive.getCounter());
    }

    /**
     * Bit hard to test. It is not guaranteed that the
     * cache will be refresh the value immediately.
     */
    @Test
    void methodShouldBeExecutedTwiceIfTimeToLiveHaveExpired() throws InterruptedException {
        expensive.size("Ho");
        Thread.sleep(CACHE_TIME_TO_LIVE_IN_MS * 2);

        expensive.size("Ho");

        assertEquals(2, expensive.getCounter());
    }

    @Test
    void cacheShouldHandleMoreParameters() {
        int expectedSum = 0;
        for (String string: STRINGS_WITH_SAME_HASCODES) {
            expectedSum += string.length();
        }

        String[] parameters = STRINGS_WITH_SAME_HASCODES.toArray(new String[0]);
        int result = expensive.sumOfSizes(parameters);

        assertEquals(expectedSum, result);
    }

    /**
     * Not a real test. Just verify that the objects
     * have same hash values.
     */
    @Test
    void verifyThatObjectsHaveSameHashValues() {
        Iterable<Integer> hashCodes = Iterables.transform(STRINGS_WITH_SAME_HASCODES,
                new Function<String, Integer>() {
                    @Override
                    public Integer apply(String object) {
                        return object.hashCode();
                    }
                });
        assertEquals(1, Sets.newHashSet(hashCodes).size());
    }

    @Test
    void cacheShouldDistinguishTwoDifferentObjectWithSameHash() {
        for(String string: STRINGS_WITH_SAME_HASCODES) {
            expensive.size(string);
        }

        assertEquals(2, expensive.getCounter());
    }

    @Test
    void shouldBeExecutedOnceWithMultipleParameters() {
        String[] params = {"First", "Second"};

        expensive.sumOfSizes(params);
        expensive.sumOfSizes(params.clone());

        assertEquals(1, expensive.getCounter());
    }

    @Test
    void shouldReceiveAnExceptionIfSomethingGoesBadInsideTheMethod() throws Exception {
        assertThrows(Exception.class, () -> expensive.dangerous("Die"));
    }

}
