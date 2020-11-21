# Bean创建 4

##### AbstractAutoWireCapableBeanFactoty中createBeanInstance方法中

```
1. boolean resolved  标记下，防止重复创建同一个bean 、有构造参数的或者工厂方法的时候进入另一个创建过程
2. boolean autowireNecessary 是否需要自动装配，构造有参数的需要
3. mbd.resolvedConstructorOrFactoryMethod 一个类中有多个构造函数，每个构造函数都有不同的参数，所以调用前需要先根据参数锁定构造函数或对应的工厂方法，主要起到的就是一个缓存作用
4.determineConstructorsFromBeanPostProcessors(beanClass, beanName) 从bean后置处理器中为自动装配寻找构造方法，有且仅有一个有参构造或者有且仅有@Autoowired注解构造
	实现了SmartInstantiationAwareBeanPostProcessor的子类
		predictBeanType预测bean的类型，主要是在bean还没有创建前我们需要获取bean的类型
		determineCandidateConstructors决定使用哪个构造方法 完成对构造函数的解析和推断
		getEarlyBeanReference解决循环依赖问题，通过此方法提前暴露一个合格的对象
		AutowiredAnnotationBeanPostProcessor这个类
		 *如果有多个Autowired，required为true，不管有没有默认构造方法，会报异常
		 * 如果只有一个Autowired，required为false，没有默认构造方法，会报警告
		 * 如果没有Autowired注解，定义了两个及以上有参数的构造方法，没有无参构造方法，就会报错？？？
		 * 其他情况都可以，但是以有Autowired的构造方法优先，然后才是默认构造方法
5. instantiateBean(beanName, mbd) 使用默认构造函数构造
	getInstantiationStrategy().instantiate(mbd, beanName, this)获取实例化策略并且进行实例化操作
		InstantiationStrategy 实例化策略接口，子类被用来根据rootBeanDefinition来创建实例对象
		方法内会再次获取构造方法，没有就获取默认构造方法，并对
		bd.resolvedConstructorOrFactoryMethod进行赋值操作，然后再创建对象
	BeanWrapperImpl包装成BeanWrapper可以编辑，就是为了进行类型转换，内部默认带的有类型转换的类
	initBeanWrapper 		
6. 当你使用构造方法注入参数的时候，就会进入autowireConstructor(beanName, mbd, ctors, args)方法进入该方法的条件是 mbd.hasConstructorArgumentValues()是true
	new ConstructorResolver(this).autowireConstructor构造器处理器选择构造方法
		从缓存中获取了构造方法之后会再尝试从缓存中获取参数没有就需要获取配置文件中配置的参数
		mbd.resolvedConstructorArgument构造器参数 preparedConstructorArguments 配置的参数
		如果从缓存中什么都没有获取到，就会获取到所有的构造方法，然后去选择一个最合适的构造方法
		AutowireUtils.sortConstructors(candidates);排序给定的构造函数，public的构造函数优
		先，参数数量降序  精髓的地方就是这个排序操作，因为当参数数量小于指定最小的参数个数时，就可以直
		接弹出了
		
		
```

实例化策略-----simple实例化策略（1.无参构造实例化2.有参构造实例化3.工厂方法实例化）---------》cglib实例化cel----动态代理对象（1.有参构造2.无参构造）

#### 跳出createBeanInstance方法之后

```
instanceWrapper.getWrappedInstance从包装类中获取原始bean
instanceWrapper.getWrappedClass获取具体的bean对象的Class属性
允许beanPostProcessor去修改合并的beanDefinition 
MergedBeanDefinitionPostProcessor后置处理器修改合并bean的定义
LifecycleMetadata 生命周期元数据 目标类 初始化方法 销毁方法 检查初始化方法 检查销毁方法
applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName)
	MergedBeanDefinitionPostProcessor resetBeanDefinition--用于在BeanDefinition被修改
	后，清除容器的缓存 postProcessMergedBeanDefinition--spring通过此方法找出所有需要注入的字
	段，同时做缓存
	InitDestroyAnnotationBeanPostProcessor postProcessMergedBeanDefinition方法
		findLifecycleMetadata 找到生命周期方法
			buildLifecycleMetadata 在反序列化之后，在销毁期间
				1.ReflectionUtils.doWithLocalMethods 如果有PostConstruct和PreDestroy注解
				的方法就添加到currInitMethods和currDestroyMethods里，包括父类，因为可能CGLIB
				动态代理，该方法里面会查看方法上是否有@PostConstruct注解存在和查看方法上是否有
				@PreDestory注解存在，并分别保存到currInitMethods，currDestroyMethods中,会一直
				循环获取直到当前类存在父类且父类补位object基类则循环对父类进行处理
					问题：这两个集合类是在哪里执行的？AbstractAutowireCapableBeanFactory类中的
					initializeBean方法中的applyBeanPostProcessorsBeforeInitialization方法
					中的bpp执行postProcessBeforeInitialization
					（InitDestroyAnnotationBeanPostProcessor类invokeInitMethods方法）
				2.return (initMethods.isEmpty() && destroyMethods.isEmpty() ? 
				this.emptyLifecycleMetadata :new LifecycleMetadata(clazz, 
				initMethods, destroyMethods))--有一个不为空就封装一个LifecycleMetadata对
				象，否则就返回空的emptyLifecycleMetadata
		metadata.checkConfigMembers 注册初始化和销毁的回调方法
			beanDefinition.registerExternallyManagedInitMethod(methodIdentifier)注册初始
			化调用方法
			beanDefinition.registerExternallyManagedDestroyMethod(methodIdentifier)注册
			销毁调用方法
	InitDestroyAnnotationBeanPostProcessor执行完成之后会调回来执行
	CommonAnnotationBeanPostProcessor类中的postProcessMergedBeanDefinition方法中处理
	@Resource的webService,ejb,Resource的属性注解
```

##### applyMergedBeanDefinitionPostProcessors方法和实例化之前的beanFactory.freezeConfiguration()冻结所有的bean定义，说明注册的bean定义将不被修改或任何进一步的处理两个是有区别的，前者是在实例化之后对bean进行最后的一次修改，后者是为了让bean在创建之前不再被修改或者改变

