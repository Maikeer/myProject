![image-20200807173136474](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807173136474.png)

以上图片红框代表创建bean过程中进入bean生命周期入口

```
* <p>Bean factory implementations should support the standard bean lifecycle interfaces
* as far as possible. The full set of initialization methods and their standard order is:
* <ol>
* <li>BeanNameAware's {@code setBeanName}
* <li>BeanClassLoaderAware's {@code setBeanClassLoader}
* <li>BeanFactoryAware's {@code setBeanFactory}
* <li>EnvironmentAware's {@code setEnvironment}
* <li>EmbeddedValueResolverAware's {@code setEmbeddedValueResolver}
* <li>ResourceLoaderAware's {@code setResourceLoader}
* (only applicable when running in an application context)
* <li>ApplicationEventPublisherAware's {@code setApplicationEventPublisher}
* (only applicable when running in an application context)
* <li>MessageSourceAware's {@code setMessageSource}
* (only applicable when running in an application context)
* <li>ApplicationContextAware's {@code setApplicationContext}
* (only applicable when running in an application context)
* <li>ServletContextAware's {@code setServletContext}
* (only applicable when running in a web application context)
* <li>{@code postProcessBeforeInitialization} methods of BeanPostProcessors
* <li>InitializingBean's {@code afterPropertiesSet}
* <li>a custom init-method definition
* <li>{@code postProcessAfterInitialization} methods of BeanPostProcessors
* </ol>
*
* <p>On shutdown of a bean factory, the following lifecycle methods apply:
* <ol>
* <li>{@code postProcessBeforeDestruction} methods of DestructionAwareBeanPostProcessors
* <li>DisposableBean's {@code destroy}
* <li>a custom destroy-method definition
```



扫描包下面的类过程发生在

![image-20200807173955863](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807173955863.png)

![image-20200807174033540](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807174033540.png)

![image-20200807174208124](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807174208124.png)





![image-20200807174226920](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807174226920.png)





![image-20200807174358309](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807174358309.png)



![image-20200807174509913](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807174509913.png)



![image-20200807174556668](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807174556668.png)



最终就是通过classLoader在当前启动类同级目录下去扫描到类的



![image-20200807175728038](D:\马士兵架构\myProject\spring-springboot源码学习\images\image-20200807175728038.png)



















