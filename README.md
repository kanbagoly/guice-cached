# guice-cached
Cache solution using Google's Guice to allow caching method calls.

## usage
Annotate your method to be cached with the `@Cached` annotation
```java
class MyClass {
    @Cached(duration = 10, timeUnit = TimeUnit.MINUTES, maxSize = 100)
    public Result expensiveCalculation(Input input) {
        ...
    }
}
```
install `CachedMethodModule` into your Guice module
```java
class MyModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new CachedMethodModule());
    }
}
```
get your `MyClass` using Guice's injector
```java
Injector injector = Guice.createInjector(new MyModule());
MyClass myObject = injector.getInstance(MyClass.class);
```
and the method calls of `expensiveCalculation`
```java
myObject.expensiveCalculation(someParameter)
```
will be cached.