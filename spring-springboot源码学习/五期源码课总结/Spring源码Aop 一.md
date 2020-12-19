

# Spring源码Aop 一

aop----动态代理-------jdk/cglib

在ioc基础上实现aop功能就是需要三个条件

1.额外添加的业务逻辑 2.位置来进行额外逻辑的执行 3.怎么筛选这个具体的位置

我们怎么做？

先编写额外的逻辑类------------------切面

##### 具体的某些方法要被执行处理-----------------切点  advisor通知器----advise

被应用在那些方法上------------------------连接点

#### 实现上面的我们需要知道那些信息

1.那些类需要进行相关的切入  expression

2.额外的逻辑处理，有几个通知消息或者说有那些逻辑可以被执行 before  after afterthrowing afterReturing around advisor通知器----advise

3.额外的处理逻辑的类是那个，也就是那个切面  aspect

#### 我需要当前这两个对象：method对象  beanFactory（beanname）

动态代理------字节码文件--------想看需要手动操作

#### 使用方式：xml和注解

1.查看配置文件的解析工作，在处理之后的beanDefinition中包含什么信息

2.对aop相关的beanDefinition进行实例化操作

##### 在进行第一个对象创建之前，就必须要把aop需要的相关对象提前准备好，因为无法预估那些对象需要动态代理

3. 在哪个步骤中可以提前实例化并且生成对应的对象 BPP

   BFPP是用来读beanFactory进行修改操作的，不会影响到后续的实例化过程

   BPP（BeanPostProcessor(before和after)）resolveBeforeInstantiation()

### 具体解析过程

```
DefaultBeanDefinitionDocumentReader中parseBeanDefinitions方法进行解析
具体看aop的解析过程
delegate.parseCustomElement(ele);
ConfigBeanDefinitionParser中parse方法中POINTCUT----ADVISOR----ASPECT解析
AopConfigUtils类中会有InfrastructureAdvisorAutoProxyCreator，AspectJAwareAdvisorAutoProxyCreator，AnnotationAwareAspectJAutoProxyCreator三个类进行动态代理创建
  parseAspect(elt, parserContext);解析aop：aspect
  Advice有五个主要的子类，我会在isAdviceNode(node, parserContext)中进行判断是否进行解析
  before  after afterthrowing afterReturing around满足这五个其中一个都会解析
  	parseAdvice(aspectName, i, aspectElement, (Element) node, parserContext, beanDefinitions, beanReferences);解析around等等
  	因为最终会生成AspectJAroundAdvice类，根据构造方法可知需要三个参数
  	Method aspectJAroundAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif所以需要提前给他准备好
  	createAdviceDefinition创建advice（AspectJAroundAdvice），并解析pointcut和注册到AspectJAroundAdvice中METHOD_INDEX，POINTCUT_INDEX，ASPECT_INSTANCE_FACTORY_INDEX分别保存构造方法需要的数据
  	前面说的创建advice之后还需要再创建advisor把它进行包装，所以接下来RootBeanDefinition advisorDefinition = new RootBeanDefinition(AspectJPointcutAdvisor.class);用来对advice进行包装，并进行环境配置
  	 before  after afterthrowing afterReturing都是一样的
```

```
createAspectComponentDefinition创建一个AspectComponentDefinition对象包装了之前创建的所有beanDefinition信息
```

```
List<Element> pointcuts = DomUtils.getChildElementsByTagName(aspectElement, POINTCUT);-----》parsePointcut解析pointCut，创建pointcutDefinition（AspectJExpressionPointcut），并把表达式赋值进去，最后注册到BeanFactory的BeanDefinition中
```

#### beanDefinition解析创建完成之后，在哪里会涉及到我们aop的操作？

```
invokeBeanFactoryPostProcessors(beanFactory);里面其实没有调用处理，如果是注解的话，这里会涉及到一个注解解析@EnableAspectJAutoProxy能够注入我们的解析类让注解@AspectJ生效
registerBeanPostProcessors(beanFactory);里面会创建org.springframework.aop.config.internalAutoProxyCreator对应的creator比如：
AspectJAwareAdvisorAutoProxyCreator或者AnnotationAwareAspectJAutoProxyCreatr
```

#### 在你的意识中，不要把配置文件和注解分开，一样的解析过程

```
<aop:aspectj-autoproxy></aop:aspectj-autoproxy>和@EnableAspectJAutoProxy这个注解效果是一样的 都是让注解@AspectJ生效
xml配置是通过org.springframework.aop.config.AopNamespaceHandler进行加载的具体aop：config和aop:aspectj-autoproxy都由谁进行处理，而注解其实也是通过impot注解引入了AspectJAutoProxyRegistrar类进行配置的
```

```
registerBeanPostProcessors(beanFactory);注册bean处理器，这里只是注册功能，真正调用的是getBean方法这个方法中会对前面注册了的org.springframework.aop.config.internalAutoProxyCreator这个类对应的AspectJAwareAdvisorAutoProxyCreator进行创建
注意如果加了aop:aspectj-autoproxy这个配置的话，创建的就是AnnotationAwareAspectJAutoProxyCreatr
```



![image-20201211211438361](D:\GitHub\myProject\spring-springboot源码学习\五期源码课总结\iamges\image-20201211211438361.png)

##### 总结：准备工作

准备beanDefinition------AspectJExpressionPointcut

​								  -------Advisor#0---#4

​							     --------- AspectJAwareAdvisorAutoProxyCreator



#### 下次进入实例化finishBeanFactoryInitialization(beanFactory);

```
preInstantiateSingletons-----》getbean-----doGetBean-----createBean-----
resolveBeforeInstantiation给BeanPostProcessors一个机会来返回代理来替代真正的实例，应用实例化前的前置处理器--------》applyBeanPostProcessorsBeforeInstantiation----postProcessBeforeInstantiation------AbstractAutoProxyCreator、AnnotationAwareAspectJAutoProxyCreator中的isInfrastructureClass和shouldSkip方法
关键方法shouldSkip方法
```

```
protected boolean shouldSkip(Class<?> beanClass, String beanName) {
   // TODO: Consider optimization by caching the list of the aspect names
   List<Advisor> candidateAdvisors = findCandidateAdvisors();这个方法是关键
   寻找所有Advisor.class的bean名字，如果存在就放入缓存，并进行创建，然后返回
   for (Advisor advisor : candidateAdvisors) {
      if (advisor instanceof AspectJPointcutAdvisor &&
            ((AspectJPointcutAdvisor) advisor).getAspectName().equals(beanName)) {
         return true;
      }
   }
   return super.shouldSkip(beanClass, beanName);
}
```

```
AnnotationAwareAspectJAutoProxyCreator类中的findCandidateAdvisors找到系统中实现了Advisor接口的bean

```

#### 有两种方式进行实例化

1.调用无参构造方法-----------实例化对象-------set设置属性值-----------填充属性

2.调用有参构造方法创建对象------------在执行之前必须要把构造器中需要的对象提前创建好----------会包含无数个嵌套的环节



##### AbstractAutoProxyCreator类中的advisedBeans变量需要记住，之后会有很多对它使用的地方

// 要跳过的直接设置FALSE
this.advisedBeans.put(cacheKey, Boolean.FALSE);

#### 从左向右创建，但是必须要把右边的对象创建成功之后左边的对象才能创建

创建AspectJPontcutAdvisor--------------》AspectJAroundAdvice--------------3个参数 Method  AspectjExpressPointcut  AspectInstanceFactory

#### AspectjPointcutAdvisor具体创建步骤

##### 1.创建AspectjPointcutAdvisor#0-4，先使用i带参构造方法进行对象的创建，但是想使用带参数的构造方法，必须先把参数对象准备好，因此要准备创建内置包含的对象AspectjPointcutAdvice

##### 2.创建AspectjPointcutAdvice，也需要使用带参数的构造方法进行创建，也需要提前准备好具体的参数对象，包含三个参数

```
AspectJExpressionPointcut  
AspectInstanceFactory SimpleBeanFactoryAwareAspectInstanceFactory
Method  MethodLocatingFactoryBean
```

##### 3.分别创建上述单个对象，上述三个对象的创建过程都是调用无参的构造方法，直接反射生成即可

```
// 以下情况符合其一即可进入
		// 1、存在可选构造方法
		// 2、自动装配模型为构造函数自动装配
		// 3、给BeanDefinition中设置了构造参数值
		// 4、有参与构造函数参数列表的参数
autowireConstructor(beanName, mbd, ctors, args);这里是有参构造方法进入处理的方法
---resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues)进入这个方法处理找到具体的构造方法-----valueResolver.resolveValueIfNecessary使用valueResolver解析出valueHolder实例的构造函数参数值所封装的对象------resolveInnerBean(argName, innerBeanName, bd)根据innerBeanName和bd解析出内部Bean对象
```

#### 以下是对上面innerBeanName的解释，具体如何生成的innerBeanName

```
//拼装内部Bean名:"(inner bean)#"+bd的身份哈希码的十六进制字符串形式
String innerBeanName = "(inner bean)" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR +
      ObjectUtils.getIdentityHexString(bd);
```

##### 上面的处理都是shouldSkip方法中findCandidateAdvisors中的super.findCandidateAdvisors()处理过程

##### 下面这个过程是处理注解的过程找到系统中使用@Aspect标注的bean，并且找到该bean中使用@Before，@After等标注的方法将这些方法封装为一个个Advisor

```
advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
```

#### 以上的方法都是在创建LogUtil过程中进入resolveBeforeInstantiation(beanName, mbdToUse)方法---进入applyBeanPostProcessorsBeforeInstantiation方法----进入ibp.postProcessBeforeInstantiation----进入shouldSkip(beanClass, beanName)进行的操作  就是查看该类是否跳过动态代理



shouldSkip方法中findCandidateAdvisors执行完成之后，接下就是执行











