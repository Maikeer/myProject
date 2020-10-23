Spring源码总结

1.接口 

加载xml-----》解析xml-------封装beanDefinition------实例化-------放入容器中------------从容器中获取



容器------Map----------                容器存放的数据格式：1.key：string  v：object 2.k：class v:object

​													3.key：string  v：beanDefinition4.key：string  v：objectFactory 



![image-20200924114013809](D:\马士兵架构\myProject\spring-springboot源码学习\五期源码课总结\iamges\image-20200924114013809.png)



1.生成beanDefinition之后可以使用用beanFactoryPostProcessor 来修改和增强beanDefinition信息，beanPostProcessor 来修改和增强bean信息，处理完成之后再使用反射技术对bean进行实例化

创建对象，

![image-20200924150551215](D:\马士兵架构\myProject\spring-springboot源码学习\五期源码课总结\iamges\image-20200924150551215.png)



### 问题：为什么要设置忽略依赖的接口ignoreDependencyInterface（）Aware和设置忽略依赖的type ignoreDependencyType的用处？



### 在初始化过程中，在不同的阶段要处理不同的工作，应该怎么办？

##### 观察者模式：监听器，监听事件，多播器（广播器）

##### 	接口

```
接口  ： 1. beanfactory
		2. Aware
		3. BeanDefinition
		4. BeanDefinitionReader
		5. BeanFactoryPostProcessor
		6. BeanPostProcessor
		7. Environment  -------------->StandardEnviroment--------->System.getEnv() 																---------->System.getProperties()
		8. FactoryBean 
```

####  beanFactory 和factoryBean的区别 

##### 1.都是用来创建对象的，当使用beanFactory必须要遵循完整的创建过程，这个过程是由spring来管理控制的

##### 2.使用FactoryBean只需要调用getObject就可以返回具体的对象，整个对象的创建过程是由用户自己来控制的，更加灵活  ，总共只有三个方法  getObject  getObjectType  isSingleton



## 第二节 AbstractApplicationContext 中refresh方法

```
1.prepareRefresh   做些准备工作 1.设置当前spring启动的时间2.设置关闭和开启的标志位3.获取当前的环境对象并设置环境对象的属性值4.设置监听器以及需要发布的事件的集合
 initPropertySources 方法上 WebApplicationContextUtils#initServletPropertySources为什么会被自动调用，是因为 StandardServletEnvironment 类覆写了initPropertySources方法并在方法中调用了initServletPropertySources方法，这个就是web项目默认创建的环境对象

2.obtainFreshBeanFactory  1.创建容器对象，DefaultListableBeanFactory 2.加载xml配置文件的属性值到当前工厂中，最重要的就是BeanDefinition
 loadBeanDefinitions 加载bean定义信息2.1创建一个xml的beanDefinitionReader并设置环境对象等属性值，
 再初始化beanDefinitionReader对象，此处设置配置文件是否进行验证（initBeanDefinitionReader），
 最后开始完成beanDefinitionReader的加载（loadBeanDefinitions）--1.以resource的方式获取配置文件的位置2.以String的方式获取配置文件的位置
 reader.loadBeanDefinitions(configLocations)---ResourcePatternResolver资源匹配解析器
 ((ResourcePatternResolver) resourceLoader).getResources(location)调用默认的resourceLoader完成具体的resource定位---再对resource数组进行一个一个的处理，并且开始对配置文件进行读取，
 ---doLoadBeanDefinitions 实际读取的操作，这个方法里面doLoadDocument方法获取xml文件的document对象，解析过程就是由documentloader完成的，从string-resource[]-resource,最终开始讲resource读取成一个document文档，根据文档的节点信息封装成一个个的beanDefinition，registerBeanDefinitions方法1.创建一个BeanDefinitionDocumentReader对象2.获取countBefore3.开始真正执行document读取并封装beanDefinition（delegate委派，委托--获取命名空间---解析xml前处理--真正解析xml parseBeanDefinitions此方法就是真正解析xml的他有1.默认标签解析和自定义标签的解析--解析xml后处理）
 ignoreDependencyInterface 略过的类，为什么要略过，因为之前添加的BPP已经对这些进行处理了
 registerResolvableDependency 设置自动装备的特殊规则，挡在进行ioc初始化的，如果有多个试下，那么使用指定的对象进行注入

3. prepareBeanFactory 准备beanFactory，初始化bean工厂，设置属性值 EL表达式 设置Aware接口 细节暂时略过
	在添加addPropertyEditorRegistrar属性编辑器注册的时候，可以总结我们需要自定义属性编辑器时候
	1.首先创建一个自定义的EditorRegistrar
	2.创建一个自定义的实现了PropertyEditorSupport的Editor，覆写setAsText方法（为什么是覆写这个方法呢？实例化对象中设置属性的方法中可以找到populateBean---->applyPropertyValues---->convertForProperty最后调用的就是setAsText方法）并在自定义EditorRegistrar中registerCustomEditors方法中进行注册		
	doRegisterEditor操作（registerCustomEditors方法具体是在哪里被调用的，请看下面调用图1）
	3.如何让spring识别我们自定义的EditorRegistrar 这个时候就需要找到CustomEditorConfigurer（它是在什么时候被执行的？invokeBeanFactoryPostProcessors方法中PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors里面进行执行的）在
	它里面有个propertyEditorRegistrars数组集合，它也是一个BFPP所以他可以让这些自定义编辑器注册可以就加入beanFactory里面，让spring能够识别他们
4. postProcessBeanFactory 扩展操作
5. invokeBeanFactoryPostProcessors 执行BFPP 实例化并且执行所有注册的BFPP，在单列对象实例化之前必须要调用
	5.1：提问，我自定义的一个BFPP在	PostProcessorRegistrationDelegate		                       .invokeBeanFactoryPostProcessors方法中是在哪里处理的，
	这个时候就要看我自定义的BFPP是通过addBeanFactoryPostProcessor注册进去的还是通过bean定义注册     到beanDefinition去的，如果是add添加进去的，就可以在if。。else方法里面处理了，
	如果是bean定义的话就可能在后面处理BeanFactoryPostProcessor的逻辑下进行处理
	5.2：ConfigurationClassPostProcessor是如何被注册，并进行执行的 首先 internalConfigurationAnnotationProcessor对应的处理类就是CCPP，所以根据这个值在调用getBean的时候获取到的就是CCPP，所以在invoke执行的时候执行的就是CCPP 
	springboot项目中是在创建context类的时候web项目是通过初始化         
	AnnotationConfigServletWebServerApplicationContext类的时候需要new 		     
	AnnotatedBeanDefinitionReader类，在new的构造方法中调用父类方法中通过
	AnnotationConfigUtils进行加载的internalConfigurationAnnotationProcessor
	非springboot项目的话就是通过解析<context:component-scan>标签解析的时候进行加载的
6. registerBeanPostProcessors 实例化并且注册BPP
7. initMessageSource 国际化设置
8. initApplicationEventMulticaster 初始化广播事件
9. onRefresh
10. registerListeners 注册监听器
11. finishBeanFactoryInitialization 实例化所有非懒加载的单列对象
    11.1 实例化转换器服务，添加嵌入的解析器，初始化代理织入的前期准备，固定配置（设置以后不再需要改变的）
    11.2 preInstantiateSingletons 实例化单列对象 RootBeanDefinition 
    11.3 getBean--->doGetBean   先从一级缓存中获取是否有对象---没有对象，先判断是否是单列对象，不是的话直接报错----获取父类容器，如果BeanDefinitionMap中也就是所有已经加载的类中不包含beanname，那么就从父容器中获取----如果不是类型检查，那么表示要创建bean，此处在集合中做一个记录，正在创建过程中----获取依赖对象bean，如果有就先进行加载实例化-----开始实例化对象createBean
    11.4 doCreateBean 实例化对象，实例化完成时候只是默认值，所以还需要填充属性---earlySingletonExposure用于解决循环依赖的问题，提前暴露------
    11.5 populateBean 填充属性方法 
    11.6 initializeBean 方法中的invokeAwareMethods方法执行aware填充--applyBeanPostProcessorsBefore 执行初始化之前的方法------invokeInitMethods 执行初始化方法-----applyBeanPostProcessorsAfterInitialization 执行初始化之后的方法
    11.7 getObjectForBeanInstance  因为bean对象可能是工厂，所以方法最终调用 object = factory.getObject();
12. finishRefresh 
```

构造器循环依赖解决不了，set循环依赖可以解决，当实例化和初始化分开的时候是可以解决循环依赖的

#### 为什么要使用三级缓存？因为getEarlyBeanReference这个方法中，BPP，因为spring中的aop是在beanPostProcessor实现的，所以当对象需要代理的时候，就需要三级缓存才能实现了

##### 2.进行自定义标签处理过程扩展

如果需要自定义标签的话，应该做以下步骤：

从源代码中可以查看

```
BeanDefinitionParserDelegate类中的parseCustomElement方法中
NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);可以总结出我们自定义是需要创建一个自己对应的解析器处理器handler

根据ContextNamespaceHandler中init方法中都是注册对应的Parser处理类，可知我们也需要创建对应的Parser处理类，并在自定义解析器处理类中init方法中添加parser处理类
```

1.创建一个对应的解析器处理类，（在init方法中添加parser类）

2.创建一个普通的spring.handlers配置文件，让应用程序能够完成加载工作

3.创建对应标签的parser类

[^调用图1]: 

```

```

反射进行值处理的两种方法：1.获取该属性对象的set方法进行赋值操作2.获取该对象的Filed的set方法设置



































































