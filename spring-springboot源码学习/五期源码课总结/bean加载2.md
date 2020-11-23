# bean加载2

getbean  dogetbean  createbean docreatebean

AbstractBeanfactory 中getbean方法中 

getObjectForBeanInstance返回对象的实例，很多人会理解不了这句话存在的意义，当你实现了FactoryBean接口的对象，需要获取具体的对象的时候就需要此方法来进行获取了

```
当对象都是单例的时候会尝试解决循环依赖的问题，但是原型模式下如果存在循环依赖的情况，那么直接抛出异常
isPrototypeCurrentlyInCreation

BeanFactory parentBeanFactory = getParentBeanFactory();
默认情况下spring中parentbean都是为null的，只有在xml或者bean定义中指定了parent才会有，那么它的意义到底在哪里？他到底是干嘛的？如果beanDefinitionMap中也就是在所有已经加载的类中不包含beanName，那么就尝试从父容器中获取

markBeanAsCreated(beanName); 如果不是做类型检查，那么表示要创建bean，此处在集合中做一个记录
getMergedLocalBeanDefinition 要做类型转换，如果是子类bean的话，会合并父类的相关属性
做了BeanDefinition对象的转换，当我们从xml文件中加载beandefinition对象的时候，封装的对象是GenericBeanDefinition,  我们可以每次都根据beanname获取新的RootBeanDefinition，但是最高效的还是放入缓存，第一次处理之后放入缓存下次从缓存中获取

getSingleton中ObjectFactory是一个函数式接口，当调用其中的getObject方法的时候，才会将实际传递的匿名内部类中的实现逻辑来执行 beforeSingletonCreation记录当前对象的加载状态，做个正在创建的标记 singletonFactory.getObject();开始进入bean对象创建，

****调用createBean方法****（AbstractAutowireCapableBeanFactory）类中----》resolveBeanClass 锁定class，根据设置的class属性或者根据className来解析class （System.getSecurityManager()获取安全管理器，并做对应的安全验证）
```

#### lookup-method  replace-method主要是解决 一个单例模式的bean下引用一个原型模式的bean  （单例引用原型）

```
关于createbean中mbdToUse.prepareMethodOverrides()详解
验证及准备覆盖的方法,lookup-method  replace-method，当需要创建的bean对象中包含了lookup-method和replace-method标签的时候，会产生覆盖操作 主要
 *pring中默认的对象都是单例的，spring会在一级缓存中持有该对象，方便下次直接获取，
 * 那么如果是原型作用域的话，会创建一个新的对象
 * 如果想在一个单例模式的bean下引用一个原型模式的bean,怎么办？
 * 在此时就需要引用lookup-method标签来解决此问题
 * 通过拦截器的方式每次需要的时候都去创建最新的对象，而不会把原型对象缓存起来
 
```

```
CGLB创建代理类的方法
private Class<?> createEnhancedSubclass(RootBeanDefinition beanDefinition) {
			// cglib规定用法，对原始class进行增强，并设置callback
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(beanDefinition.getBeanClass());
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			if (this.owner instanceof ConfigurableBeanFactory) {
				ClassLoader cl = ((ConfigurableBeanFactory) this.owner).getBeanClassLoader();
				enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(cl));
			}
			// 过滤，自定义逻辑来指定调用的callback下标
			enhancer.setCallbackFilter(new MethodOverrideCallbackFilter(beanDefinition));
			// 只是产生class就直接指定callback类型，跟上面指定的callbackFilter对应
			enhancer.setCallbackTypes(CALLBACK_TYPES);
			return enhancer.createClass();
		}
```

新生状态-----开始创建-----创建过程中-----创建结束

（实例化----填充属性----执行aware接口方法----执行init方法----执行后置处理的方法）都属于创建过程中

#### Bean创建3 接上面的 ****调用createBean方法****

```
上面讲到了mbdToUse.prepareMethodOverrides方法，主要验证及准备覆盖的方法,lookup-method  replace-method  解决 单例引用原型的问题
resolveBeforeInstantiation(beanName, mbdToUse) 给BeanPostProcessors一个机会来返回代理来替代真正的实例，应用实例化前的前置处理器 这个方法里面如何执行了创建出了代理bean的话就不会继续向下执行doCreateBean方法了，也就是自定义实现了 InstantiationAwareBeanPostProcessor接口
doCreateBean(beanName, mbdToUse, args)实际创建bean的调用-----createBeanInstance方法 根据执行bean使用对应的策略创建新的实例，如，工厂方法，构造函数主动注入、简单初始化------resolveBeanClass(mbd, beanName)确认需要创建的bean实例的类可以实例化------mbd.getInstanceSupplier判断当前beanDefinition中是否包含实例供应器，此处相当于一个回调方法，利用回调方法来创建bean，默认是没有的，那么我们如何把InstanceSupplier设置进入RootBeanDefinition的？？？ 通过BFPP设置进去------mbd.getFactoryMethodName 如果工厂方法不为空则使用工厂方法初始化策略-------instantiateUsingFactoryMethod就是逻辑判断区分静态方法工厂和实例化工厂，等等逻辑判断，排除各种情况最后选择一种最合适的方式来创建对象，createArgumentArray这个方法就是计算参数差异值的，最后是通过参数差异值比较保存最小的，选择出来最合适的方式创建-------bw.setBeanInstance(instantiate(beanName, mbd, factoryBean, factoryMethodToUse, argsToUse))先通过instantiate创建对象并设置-----通过这个方法创建的对象factoryMethod.invoke使用factoryMethod实例化对象
	
比较复杂的通用方法-----JDBC-----反射创建增删改查的方法-----ORM对象关系映射----BaseMapper
```

创建对象的方法有几种？

1. 自定义BeanPostProcessor，生成代理对象 InstantiationAwareBeanPostProcessor接口

2. 通过反射创建对象
3. 通过FactoryBean创建对象
4. 通过FactoryMethod创建对象
5. 通过supplier创建对象

supplier和factorybean很相似，一个是抽象出一个接口规范，所有的对象必须通过getObject方法获取（接口规范实现）

另一个是随便定义创建对象的方法，不止局限于getObject（只是BeanDefinition的属性值）



#### Bean创建4 过程





































