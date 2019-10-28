package com.kanbagoly.guice.cached;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(cached.getNumberOfExecution()).isEqualTo(1);
    }

    @Test
    void methodShouldBeExecutedOnlyOnceIfCalledWithTheSameParameters() {
        cached.size("Hi");

        cached.size("Hi");

        assertThat(cached.getNumberOfExecution()).isEqualTo(1);
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

        assertThat(cached.getNumberOfExecution()).isEqualTo(2);
    }

    @Test
    void cacheShouldHandleMoreParameters() {
        int expectedSum = 0;
        for (String string: STRINGS_WITH_SAME_HASH_CODES) {
            expectedSum += string.length();
        }

        String[] parameters = STRINGS_WITH_SAME_HASH_CODES.toArray(new String[0]);
        int result = cached.sumOfSizes(parameters);

        assertThat(result).isEqualTo(expectedSum);
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

        assertThat(cached.getNumberOfExecution()).isEqualTo(2);
    }

    @Test
    void shouldBeExecutedOnceWithMultipleParameters() {
        String[] params = {"First", "Second"};

        cached.sumOfSizes(params);
        cached.sumOfSizes(params.clone());

        assertThat(cached.getNumberOfExecution()).isEqualTo(1);
    }

    @Test
    void shouldReceiveAnExceptionIfSomethingGoesBadInsideTheMethod() {
        assertThatThrownBy(() -> cached.dangerous("Die"))
                .isInstanceOf(ExecutionException.class);
    }

    public static class CachedMethods {

        private int numberOfExecutions = 0;

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer size(String string) {
            ++numberOfExecutions;
            return string.length();
        }

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer sumOfSizes(String... strings) {
            ++numberOfExecutions;
            int sum = 0;
            for (String string: strings) {
                sum += string.length();
            }
            return sum;
        }

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer meaningOfLife() {
            ++numberOfExecutions;
            return 42;
        }

        @Cached(duration = TIME_TO_LIVE_IN_MS, timeUnit = MILLISECONDS, maxSize = CACHE_SIZE)
        public Integer dangerous(String value) {
            throw new RuntimeException();
        }

        public int getNumberOfExecution() {
            return numberOfExecutions;
        }
    }

    private static void assertHaveSameHashCodes(List<String> values) {
        assertThat(values.stream().map(String::hashCode).collect(toSet())).hasSize(1);
    }

}
