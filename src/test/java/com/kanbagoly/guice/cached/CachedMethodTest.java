package com.kanbagoly.guice.cached;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CachedMethodTest {

    private static final int TIME_TO_LIVE_IN_MS = 200;

    private static final int CACHE_SIZE = 5;

    private static final List<String> STRINGS_WITH_SAME_HASH_CODES = Arrays.asList(
            "Microcomputers: the unredeemed lollipop...",
            "Incentively, my dear, I don't tessellate a derangement.");

    private static Injector injector;
    private CachedMethods cached;

    @BeforeAll
    static void createInjector() {
        injector = Guice.createInjector(new CachedMethodModule());
    }

    @BeforeEach
    void setUp() {
        cached = injector.getInstance(CachedMethods.class);
    }

    @Test
    void methodWithoutParameterShouldBeAbleToUseCache() {
        cached.meaningOfLife();

        cached.meaningOfLife();

        assertEquals(1, cached.getNumberOfCalls());
    }

    @Test
    void methodShouldBeExecutedOnlyOnceIfCalledWithTheSameParameters() {
        cached.size("Hi");

        cached.size("Hi");

        assertEquals(1, cached.getNumberOfCalls());
    }

    /**
     * Bit hard to test. It is not guaranteed that the
     * cache will be refresh the value immediately.
     */
    @Test
    void methodShouldBeExecutedTwiceIfTimeToLiveHaveExpired() throws InterruptedException {
        cached.size("Ho");
        Thread.sleep(TIME_TO_LIVE_IN_MS * 2);

        cached.size("Ho");

        assertEquals(2, cached.getNumberOfCalls());
    }

    @Test
    void cacheShouldHandleMoreParameters() {
        int expectedSum = 0;
        for (String string: STRINGS_WITH_SAME_HASH_CODES) {
            expectedSum += string.length();
        }

        String[] parameters = STRINGS_WITH_SAME_HASH_CODES.toArray(new String[0]);
        int result = cached.sumOfSizes(parameters);

        assertEquals(expectedSum, result);
    }

    /**
     * Not a real test. Just verify that the objects
     * have same hash values.
     */
    @Test
    void verifyThatObjectsHaveSameHashValues() {
        assertHaveSameHashCodes(STRINGS_WITH_SAME_HASH_CODES);
    }

    @Test
    void cacheShouldDistinguishTwoDifferentObjectWithSameHash() {
        for(String string: STRINGS_WITH_SAME_HASH_CODES) {
            cached.size(string);
        }

        assertEquals(2, cached.getNumberOfCalls());
    }

    @Test
    void shouldBeExecutedOnceWithMultipleParameters() {
        String[] params = {"First", "Second"};

        cached.sumOfSizes(params);
        cached.sumOfSizes(params.clone());

        assertEquals(1, cached.getNumberOfCalls());
    }

    @Test
    void shouldReceiveAnExceptionIfSomethingGoesBadInsideTheMethod() throws Exception {
        assertThrows(Exception.class, () -> cached.dangerous("Die"));
    }

    public static class CachedMethods {

        private int numberOfCalls = 0;

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer size(String string) {
            ++numberOfCalls;
            return string.length();
        }

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer sumOfSizes(String... strings) {
            ++numberOfCalls;
            int sum = 0;
            for (String string: strings) {
                sum += string.length();
            }
            return sum;
        }

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer meaningOfLife() {
            ++numberOfCalls;
            return 42;
        }

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = TimeUnit.MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer dangerous(String value) {
            throw new RuntimeException();
        }

        public int getNumberOfCalls() {
            return numberOfCalls;
        }
    }

    private static void assertHaveSameHashCodes(List<String> values) {
        assertThat(values.stream().map(String::hashCode).collect(toSet())).hasSize(1);
    }

}
