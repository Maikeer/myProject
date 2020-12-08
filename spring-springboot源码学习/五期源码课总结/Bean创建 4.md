

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

# Bean创建过程 5

加载注解 的相关解析配置工作

CommonAnnotationBeanPostProcessor处理@Resource----》InitDestroyAnnotationBeanPostProcessor处理@PostConstruct注解@PreDestory注解

```
AutowiredAnnotationBeanPostProcessor 处理@AutoWired注解
```

为什么要增加这个applyMergedBeanDefinitionPostProcessors呢？有何意义？

这里就是对注解的解析工作进行解析，并把信息保存下来，在接下来的初始化过程中就可以获取对应信息并进行赋值。

### 这里xml中property配置了，字段上又加了Autowired的时候，是怎么处理的可以自己测试一下？ 未测试  测试如果xml和autowired的id是不一样就会报错，一样会覆盖，使用xml中的

##### 5.很多同学在刚刚的步骤中说到了初始化，初始化包含了那些环节

a填充属性

b执行aware接口对应的方法

c执行beanpostprocessor中的before方法

d执行init-method

e执行beanPostProcessor中的after方法

上述步骤执行完成之后是为了获取到一个完整的成品对象，但是在初始化前我们能确定哪一个对象需要生成代理对象吗？

不能确定，而且我们三级缓存只是一个回调机制，所以能否把所有的bean所需的创建代理对象的lambda表达式都放到三级缓存中？

可以将所有的bean需要创建的代理对象的lambda表达式放到三级缓存中，后续如果我需要调用，直接从三级缓存中调用执行即可，如果不需要，在生成完整对象之后可以在三级缓存中把lanmda表达式给清除掉

##### 6.我在什么时候生成具体的代理对象？

a:在进行属性注入的时候，调用该对象生成的时候检测是否需要被代理，如果需要，直接创建代理对象

b：在整个过程中，没有其他的对象有当前对象的依赖，那么在生成最终的完整对象之前生成代理对象即可(beanpostprocessor中的after方法中)

提前暴露对象---二级缓存----只实例化但未初始化的对象

三级缓存查找顺序？1-----2-----3

有没有可能我直接从三级缓存跳过2级直接到达一级缓存？有可能

### 如果单纯为了解决循环依赖的问题，那么使用二级缓存足够解决问题，三级缓存存在的意义是为了代理，如果没有代理对象，二级缓存足以解决问题

# Bean创建过程 6

上集回顾：

注解处理 @PostConstruct注解@PreDestory    

循环依赖的处理与理解

## 本集内容

填充属性----给属性赋值

分类------基本数据类型-----直接完成赋值操作---四类八种-----类型转换

​       --------引用类型-------从容器中获取具体的对象值，如果有，直接赋值，如果没有创建-----》注入方式

​										------不注入

​		注入的方式			-------按照类型完成注入

​										--------按照名称完成注入

​										--------按照构造器进行注入

------集合属性 数组 list map set properties  应该既可能有基本数据类型 也可能有引用类型

```
populateBean(beanName, mbd, instanceWrapper)对bean的属性进行填充，将各个属性值注入，其中，可能存在依赖于其他bean的属性，则会递归初始化依赖的bean
	if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) 如果mdb是不是'syntheic' 且 工厂拥有InstiationAwareBeanPostProcessor  就进去执行方法
	PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);PropertyValues：包含以一个
	或多个PropertyValue对象的容器，通常包括针对特定目标Bean的一次更新//如果mdb有PropertyValues就获取其PropertyValues
	需要在xml中配置 <property name="province" value="河北"></property>才会有值
	mbd.getResolvedAutowireMode()获取 mbd 的 自动装配模式 根据获取到的值判断走根据类型还是根据名字自动注入
	autowireByName(beanName, mbd, bw, newPvs);通过bw的PropertyDescriptor属性名，查找出对应的Bean对象，将其添加到
	newPvs中unsatisfiedNonSimpleProperties(mbd, bw);获取bw中有setter方法 && 非简单类型属性 && 
	mbd的PropertyValues中没有该pd的属性名的 PropertyDescriptor 属性名数组---就是一个熟悉筛选过程如果 
	pd有写入属性方法，可以获取到一个set**方法 && 该pd不是被排除在依赖项检查之外 && pvs没有该pd的属性名 
	&& pd的属性类型不是"简单值类型"
			pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && 
		!pvs.contains(pd.getName())&&!BeanUtils.isSimpleProperty(pd.getPropertyType())
		遍历属性名
		containsBean(propertyName)---如果该bean工厂有propertyName的beanDefinition或外部注册的
		singleton实例获取该工厂中propertyName的bean对象  Object bean = getBean(propertyName);
		注册propertyName与beanName的依赖关系 registerDependentBean(propertyName, beanName);问？为什么要保存这个依赖关系，就是为了下次或者多次创建的时候可以不用再次创建已经创建过的依赖
	autowireByType(beanName, mbd, bw, newPvs);通过bw的PropertyDescriptor属性类型，查找出对应的Bean对象，将其添加到newPvs中
	获取工厂的自定义类型转换器getCustomTypeConverter() 如果有就使用自定义的类型转换器
		 unsatisfiedNonSimpleProperties(mbd, bw);获取bw中有setter方法 && 非简单类型属性 && mbd
		 的PropertyValues中没
		 有该pd的属性名的 PropertyDescriptor 属性名数组
		 遍历属性名数组
		 1.bw.getPropertyDescriptor(propertyName)从bw中获取propertyName对应的
		 PropertyDescriptor
		 2.//eager为true时会导致初始化lazy-init单例和由FactoryBeans(或带有"factory-bean"引用的工
		 厂方法)创建 的对象以进行类型检查boolean eager = !(bw.getWrappedInstance() instanceof 
		 PriorityOrdered);
		 3.new AutowireByTypeDependencyDescriptor(methodParam, eager)将 methodParam 封装包
		 装成AutowireByTypeDependencyDescriptor对象AutowireByTypeDependencyDescriptor:根据类
		 型依赖自动注入的描述符，重写了 getDependencyName() 方法，使其永远返回null
		 4.resolveDependency(desc, beanName, autowiredBeanNames, converter);根据据desc的依
		 赖类型解析出与descriptor所包装的对象匹配的候选Bean对象
		 	4.1 descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());获取工
		 	厂的参数名发现器，设置到descriptor中。使得descriptor初始化基础方法参数的参数名发现。此时，
		 	该方法实际上并没有尝试检索参数名称；它仅允许发现再应用程序调用getDependencyName时发生
		 	4.2 getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
					descriptor, requestingBeanName);尝试获取延迟加载代理对象
			4.3 doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter)解析出与descriptor所包装的对象匹配的候选Bean对象
				4.3.1 ConstructorResolver.setCurrentInjectionPoint(descriptor);设置新得当前切入点对象，得到旧的当前切入点对象
				4.3.2 resolveMultipleBeans(descriptor, beanName, autowiredBeanNames,typeConverter)尝试针对desciptor所包装的对象类型是[stream,数组,Collection类型且对象类型是接口,Map]的情况，进行解析与依赖类型匹配的候选Bean对象 针对desciptor所包装的对象类型是[stream,数组,Collection类型且对象类型是接口,Map]的情况，进行解析与依赖类型匹配的 候选Bean对象，并将其封装成相应的依赖类型对象
				4.3.3 findAutowireCandidates(beanName, type, descriptor);尝试与type匹配的唯一候选bean对象，查找与type匹配的候选bean对象,构建成Map，key=bean名,val=Bean对象
3 !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()	如果mdb是不是'syntheic'且工厂拥有InstiationAwareBeanPostProcessor
4.for (BeanPostProcessor bp : getBeanPostProcessors())遍历工厂内的所有后置处理器，就是处理之前解析@Autowired 注解@PostConstruct注解@PreDestory注解的beanDefinition赋值调用postProcessProperties方法进入inject方法
如果工厂拥有InstiationAwareBeanPostProcessor,那么处理对应的流程，主要是对几个注解的赋值工作包含的两个关键子类是CommonAnnoationBeanPostProcessor,AutowiredAnnotationBeanPostProcessor
5.checkDependencies(beanName, mbd, filteredPds, pvs);检查依赖项：主要检查pd的setter方法需要赋值时,pvs中有没有满足其pd的需求的属性值可供其赋值
6.applyPropertyValues(beanName, mbd, bw, pvs);应用给定的属性值，解决任何在这个bean工厂运行时其他bean的引用。必须使用深拷贝，所以我们 不会永久地修改这个属性
```

### autowireByType 和autowireByName，bytype可以出来properties  和map对象填充，byname不行

#### 以下就是一个实例在for (BeanPostProcessor bp : getBeanPostProcessors())循环中AutowiredAnnotationBeanPostProcessor执行postProcessProperties方法的时候就会处理@Autowired自动注入的逻辑，会先创建personController------》注入personService-------》创建PersonService-------注入PersonDao------》创建PersonDao----再依次返回

```
上面在执行过程中会进入这个方法，创建bean然后又回到populateBean，从而达到这个依次注入的目的
//如果instanceCandidate是Class实例
if (instanceCandidate instanceof Class) {
   //让instanceCandidate引用 descriptor对autowiredBeanName解析为该工厂的Bean实例
   instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
}
```

![image-20201126223730785](D:\GitHub\myProject\spring-springboot源码学习\images\image-20201126223730785.png)

![image-20201126223803789](D:\GitHub\myProject\spring-springboot源码学习\images\image-20201126223803789.png)





作业：完成一个自定义注解，实现autowired的功能，1.先让spring能够识别注解2.后续能够处理注解注入属性

总结：这集课主要是讲的populateBean 填充属性，用来自 BeanDefinition的属性值填充给定的BeanWrapper中的bean实例

# Bean创建过程 7

##### applyPropertyValues(beanName, mbd, bw, pvs);应用给定的属性值，解决任何在这个bean工厂运行时其他bean的引用。必须使用深拷贝，所以我们 不会永久地修改这个属性----具体细节

​	PropertyValues pvs必须要在xml配置中带有<property>标签才会有pvs

先创建一个MutablePropertyValues（PropertyValues接口的默认实现。允许对属性进行简单操作，并提供构造函数来支持从映射 进行深度复制和构造），然后再把传入的pvs赋值给他，判断mpvs是否已经转换，如果已经转换就直接setPropertyValues然后返回，没有转换就回去getPropertyValueList赋值给original，接着用户自定义类型转换器，没有就把bw赋值给converter

2.创建BeanDefinitionValueResolver在bean工厂实现中使用Helper类，它将beanDefinition对象中包含的值解析为应用于 目标bean实例的实际值

3.List<PropertyValue> deepCopy  创建一个深拷贝，解析任何值引用，为了不影响其他对象

4. resolveNecessary是否还需要解析标记

5. ```
   遍历属性，将属性转换为对应类的对应属性的类型 original
   ```

6.valueResolver.resolveValueIfNecessary(pv, originalValue) 交由valueResolver根据pv解析出originalValue所封装的对象

7.可转换标记: propertyName是否bw中的可写属性 && prepertyName不是表示索引属性或嵌套属性（如果propertyName中有'.'||'['就认为是索引属性或嵌套属性）

```
boolean convertible = bw.isWritableProperty(propertyName) &&
      !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
```

8.convertForProperty(resolvedValue, propertyName, bw, converter);将resolvedValue转换为指定的目标属性对象

9.各种判断之后放入deepCopy中

10.bw.setPropertyValues(new MutablePropertyValues(deepCopy));按原样使用deepCopy构造一个新的MutablePropertyValues对象然后设置到bw中以对bw的属性值更新

### pupulateBean具体执行

----1，调用postProcessAfterInstantiation方法完成属性赋值工作，可以直接终止后续的值处理工作，也可以让后续的属性完成覆盖操作，取决于自己----postProcessAfterInstantiation

----2.根据配置文件的autowired属性来决定使用名称注入还是类型注入---aotowireByName和autowireByType

----3.将对象中定义的@autowired注解进行解析，并完成对象或者属性注入----postProcessProperties---AutowiredAnnotationBeanPostProcessor

---4.根据property标签定义的属性值，完成各种属性值的解析和赋值工作-----applyPropertyValues

#### initializeBean 执行初始化逻辑

 exposedObject = initializeBean(beanName, exposedObject, mbd)

1.执行调用Aware接口对应的方法-----BeanNameAware、BeanClassLoaderAware，BeanFactoryAwaare

2.执行before的初始化方法----ApplicationContextAwareProcessor，CommonAnnotationBeanPostProcessor，InitDestroyAnnotationBeanPostProcessor---@PostConstruct @PreDestroy

3.调用执行init-method------实现了InitializingBean接口之后调用afterPropertiesSet方法

​										------调用执行用户自定义初始化方法init-method

4.执行after的初始化方法----AbstractAutoProxyCreator---AOP

```
invokeAwareMethods(beanName, bean);Aware接口处理器，调用BeanNameAware、BeanClassLoaderAware、beanFactoryAware
为什么这里只处理这三个？因为在创建DefaultListableBeanFactory类时候父类忽略了要依赖的接口
```

```
applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);将BeanPostProcessors应用到给定的现有Bean实例，调用它们的postProcessBeforeInitialization初始化方法。  返回的Bean实例可能是原始Bean包装器
```

```
invokeInitMethods 调用初始化方法，先调用bean的InitializingBean接口方法，后调用bean的自定义初始化方法
调用bean的afterPropertiesSet方法 ((InitializingBean) bean).afterPropertiesSet();
在bean上调用指定的自定义init方法 invokeCustomInitMethod(beanName, bean, mbd);
```

```
applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);将BeanPostProcessors应用到给定的现有Bean实例，调用它们的postProcessAfterInitialization方法。返回的Bean实例可能是原始Bean包装器
```

# bean的创建过程8及循环依赖

```
AbstractAutowireCapableBeanFactory类 740行开始
```

```
earlySingletonExposure这个值是在前面添加三级缓存的时候的判断值
Object earlySingletonReference = getSingleton(beanName, false); 从缓存中获取具体的对象
// earlySingletonReference只有在检测到有循环依赖的情况下才会不为空
if (earlySingletonReference != null) 什么时候这个值不为null？
```

```
// 注册bean对象，方便后续在容器销毁的时候销毁对象
registerDisposableBeanIfNecessary(beanName, bean, mbd);
     registerDisposableBean(beanName,new DisposableBeanAdapter(bean, beanName, 
     mbd, getBeanPostProcessors(), acc));
     			// 注册一个一次性Bean实现来执行给定Bean的销毁工作：
    			 DestructionAwareBeanPostProcessors 一次性Bean接口，自定义销毁方法。
				// DisposableBeanAdapter：实际一次性Bean和可运行接口适配器，对给定Bean
				实例执行各种销毁步骤
				// 构建Bean对应的DisposableBeanAdapter对象，与beanName绑定到 注册中心
				的一次性Bean列表中
```

#### 上面创建bean完成之后，就回到了DefaultSingletonBeanRegistry类 351行

```
afterSingletonCreation(beanName);创建单例后的回调,默认实现将单例标记为不在创建中这个是和前面的beforeSingletonCreation(beanName);创建单例之前的回调,默认实现将单例注册为当前正在创建中对应

```

#### bean生命周期

![](D:\GitHub\myProject\spring-springboot源码学习\五期源码课总结\iamges\Bean的生命周期.jpg)



#### 循环依赖问题

![](D:\GitHub\myProject\spring-springboot源码学习\五期源码课总结\iamges\循环依赖问题.jpg)







































































