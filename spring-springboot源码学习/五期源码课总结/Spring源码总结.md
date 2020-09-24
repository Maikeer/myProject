Spring源码总结

1.接口 

 beanFactory 和factoryBean的区别  都是用来创建对象的，当使用beanFactory必须要遵循完整的创建过程，这个过程是由spring来管理控制的

而使用FactoryBean只需要调用getObject就可以返回具体的对象，整个对象的创建过程是由用户自己来控制的，更加灵活  ，总共只有三个方法  getObject  getObjectType  isSingleton