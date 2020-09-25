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

2.obtainFreshBeanFactory  1.创建容器对象，DefaultListableBeanFactory 2.加载xml配置文件的属性值到当前工厂中，最重要的就是BeanDefinition

3. prepareBeanFactory 准备beanFactory，初始化bean工厂，设置属性值 EL表达式 设置Aware接口 细节暂时略过
4. postProcessBeanFactory 扩展操作
5. invokeBeanFactoryPostProcessors 执行BFPP 实例化并且执行所有注册的BFPP，在单列对象实例化之前必须要调用
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





















