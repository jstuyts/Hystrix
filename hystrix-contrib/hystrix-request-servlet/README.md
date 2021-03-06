# Hystrix Request Servlet Filters

This module contains functional examples for a J2EE/Servlet environment that initialize and uses  [HystrixRequestContext](https://github.com/Netflix/Hystrix/tree/master/hystrix-core/src/main/java/com/netflix/hystrix/strategy/concurrency/HystrixRequestContext.java).

You can use this module as is or model your own implementation after it as these classes are very basic.

If using a framework that doesn't use Servlets, or a framework with other lifecycle hooks you may need to implement your own anyways.

# Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22hystrix-request-servlet%22).

Example for Maven:

```xml
<dependency>
    <groupId>com.netflix.hystrix</groupId>
    <artifactId>hystrix-request-servlet</artifactId>
    <version>1.1.2</version>
</dependency>
```

and for Ivy:

```xml
<dependency org="com.netflix.hystrix" name="hystrix-request-servlet" rev="1.1.2" />
```

# Installation

## [HystrixRequestContextServletFilter](https://github.com/Netflix/Hystrix/tree/master/hystrix-contrib/hystrix-request-servlet/src/main/java/com/netflix/hystrix/contrib/requestservlet/HystrixRequestContextServletFilter.java)

This initializes the [HystrixRequestContext](https://github.com/Netflix/Hystrix/tree/master/hystrix-core/src/main/java/com/netflix/hystrix/strategy/concurrency/HystrixRequestContext.java) at the beginning of each HTTP request and then cleans it up at the end.

You install it by adding the following to your web.xml:

```xml
  <filter>
    <display-name>HystrixRequestContextServletFilter</display-name>
    <filter-name>HystrixRequestContextServletFilter</filter-name>
    <filter-class>com.netflix.hystrix.contrib.requestservlet.HystrixRequestContextServletFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>HystrixRequestContextServletFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```
