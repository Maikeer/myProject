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

1.prepareRefresh   做些准备工作 1.设置当前spring启动的时间2.设置关闭和开启的标志位3.获取当前的环境对象并设置环境对象的属性值4.设置监听器以及需要发布的事件的集合

2.obtainFreshBeanFactory   告诉子类刷新内部bean工厂并返回一个ConfigurableListableBeanFactory 可配置，可枚举bean信息的bean工厂











