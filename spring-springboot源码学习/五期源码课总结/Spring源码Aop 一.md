

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
AnnotationAwareAspectJAutoProxyCreator类中的findCandidateAdvisors
```





























