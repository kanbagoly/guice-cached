# guice-cached
Cache solution using Google's Guice to allow caching method calls.

## usage
Install `CachedMethodModule` into your Guice module
```java
class MyModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new CachedMethodModule());
    }
}
```
annotate your method to be cached with the `@Cached` annotation
```java
class MyClass {
    @Cached(duration = 10, timeUnit = TimeUnit.MINUTES, maxSize = 100)
    public Result expensiveCalculation(Input input) {
        ...
    }
}
```
and get your `MyClass` using Guice's injector and the annotated method calls will be cached
```java
Injector injector = Guice.createInjector(new MyModule());
MyClass cached = injector.getInstance(MyClass.class);
``` 