# guice-cached
Cache solution using Google's Guice to allow caching method calls.

## usage

Install `CachedMethodModule` into your module like
```java
class MyModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new CachedMethodModule());
    }
}
```
and annotate your method to be cached with the `@Cached` annotation like
```java
class MyClass {
        @Cached(duration = 10, timeUnit = TimeUnit.MINUTES, maxSize = 100)
        public Result expensiveCalculation(Input input) {
            ...
        }
}
```
and just get your `MyClass` using Guice and the method calls will be cached
```java
Injector injector = Guice.createInjector(new MyModule());
MyClass cached = injector.getInstance(MyClass.class);
``` 