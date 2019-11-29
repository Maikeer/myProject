
# JVM
01_ClassFileFormat.md
## 1：JVM基础知识

1. 什么是JVM
   class文件，java类库被classloader加载到内存中会调用 JIT即时编译器或者字节码解释器 进行编译解释 然后调用执行引擎 使用OS硬件进行执行
2. 常见的JVM

## 2：ClassFileFormat
 ****课上同学说的64位的时候 long duoble前面加了volatile之后，在多线程问题的时候加了这个修饰符之后，他的写和读取操作都是原子性的
 ，比如 volatile long l=88;他翻译出来的的putfield指令就是原子性的操作了 java虚拟机底层就会把它当做原子性的来处理
 cpu层面保证原子性的操作：最早实现是总线锁--之后是MESI保证的


## 3：类编译-加载-初始化

hashcode
锁的信息（2位 四种组合）
GC信息（年龄）
如果是数组，数组的长度

## 4：JMM

new Cat()
pointer -> Cat.class
寻找方法的信息

## 5：对象

1：句柄池 （指针池）间接指针，节省内存
2：直接指针，访问速度快

## 6：GC基础知识

栈上分配
TLAB（Thread Local Allocation Buffer）
Old
Eden
老不死 - > Old

## 7：GC常用垃圾回收器

new Object()
markword          8个字节
类型指针           8个字节
实例变量           0
补齐                  0		
16字节（压缩 非压缩）
Object o
8个字节 
JVM参数指定压缩或非压缩

--------------------------------------------------------------------------------------------------------------
02_ClassLodingLinkingInitializing.md
## 3：类加载-初始化

1. 加载过程
   1. Loading
      
      1. 双亲委派，主要出于安全来考虑
      
      2. LazyLoading 五种情况
      
         1. –new getstatic putstatic invokestatic指令，访问final变量除外
      
            –java.lang.reflect对类进行反射调用时
      
            –初始化子类的时候，父类首先初始化
      
            –虚拟机启动时，被执行的主类必须初始化
      
            –动态语言支持java.lang.invoke.MethodHandle解析的结果为REF_getstatic REF_putstatic REF_invokestatic的方法句柄时，该类必须初始化
      
      3. ClassLoader的源码
      
         1. findInCache -> parent.loadClass -> findClass()
      
      4. 自定义类加载器
      
         1. extends ClassLoader
         2. overwrite findClass() -> defineClass(byte[] -> Class clazz)
         3. 加密
         4. <font color=red>第一节课遗留问题：parent是如何指定的，打破双亲委派，学生问题桌面图片</font>
            1. 用super(parent)指定
            2. 双亲委派的打破
               1. 如何打破：重写loadClass（）
               2. 何时打破过？
                  1. JDK1.2之前，自定义ClassLoader都必须重写loadClass()
                  2. ThreadContextClassLoader可以实现基础类调用实现类代码，通过thread.setContextClassLoader指定
                  3. 热启动，热部署
                     1. osgi tomcat 都有自己的模块指定classloader（可以加载同一类库的不同版本）
               打破双亲委派模式可以自己写个classloader，重新classloader方法，再里面直接load而不去父loader查询，调用defineclass方法
      5. 混合执行 编译执行 解释执行
      
         1. 检测热点代码：-XX:CompileThreshold = 10000
      
   2. Linking 
      1. Verification
         1. 验证文件是否符合JVM规定
      2. Preparation
         1. 静态成员变量赋默认值
      3. Resolution 解析
         1. 将类、方法、属性等符号引用解析为直接引用
            常量池中的各种符号引用解析为指针、偏移量等内存地址的直接引用
      这个里面要注意两个方面就是load一个class的时候，静态变量会经过两个状态就是---默认值---之后才是赋予初始值
                                 load完成之后new一个对象的时候，里面的成员变量也会经过默认值----之后才是赋予初始值，当然最开始会有一个
                                 申请内存的过程
   3. Initializing
   
      1. 调用类初始化代码 <clinit>，给静态成员变量赋初始值
   
2. 小总结：

   1. load - 默认值 - 初始值
   2. new - 申请内存 - 默认值 - 初始值
   -----------------------------------------------------------------------------------------
   03_01_JMM.md
   # JMM
   java memory model java内存模型
   存储器层次结构
   参考图  计算单元与寄存器----》L1-------》L2---------》L3--------》main memory 越来越慢
   L1，L2访问L3cache的时候，如何保证L3的数据一致性问题，在L2当中有两个线程去访问的时候，老cpu是L2通过bus总线去访问L3，加个总线锁
   但是现在cpu数据一致性的实现是用的MESI + bus总线锁 缓存锁实现的 参考下面的 链接
   mesi cache一致性协议
   缓存内容会有四种标记状态 1.modefied 我这个cpu修改了别人访问的缓存 2. exclusive 独自享有 3.shared 共享标记 也就是两个线程在读 4.invalid 无效标记 一个线程读，另一个线程修改了
   缓存锁实现之一有些
   四种状态的标记是在另外一个地方不是再缓存里
## 硬件层数据一致性

协议很多

intel 用MESI

https://www.cnblogs.com/z00377750/p/9180644.html

现代CPU的数据一致性实现 = 缓存锁(MESI ...) + 总线锁

读取缓存以cache line（缓存行）为基本单位，目前64bytes

位于同一缓存行的两个不同数据 比如 x y在同一个缓存行，
被两个不同CPU锁定 比如 线程1读取了 x y的缓存行 线程2也读取了 x y 的缓存行，但是线程1修改了x的值，他会通知线程2进行更新，线程2修改y，也会通知线程1，但是线程1关注的x  线程2关注的y，这个时候他们相互影响了
产生互相影响的伪共享问题

伪共享问题：JUC/c_028_FalseSharing

使用缓存行的对齐（也就是消耗了内存空间换取了时间，具体操作就是一类中提前加载了一个缓存行64k剩余的空间 56k 7个long值，保证两个线程中加载的类不在同一缓存行）能够提高效率

## 乱序问题

CPU为了提高指令执行效率，会在一条指令执行过程中（比如去内存读数据（慢100倍）），去同时执行另一条指令，前提是，两条指令没有依赖关系

https://www.cnblogs.com/liushaodong/p/4777308.html
WCBuffer
# 写操作
也可以进行合并  也就是说 cpu计算完成之后，先给L1没有名字缓存会再给L2，因为L2写的速度很慢，当在写的过程中另一个线程经过cpu计算完成之后
又再次写出值的时候，这个时候cpu会把前一次的计算结果和这次的计算结果在一个新的缓存区中进行合并之后再发送给L2进行输出
合并写的buffer只有四个位置，当四个位置写满之后就会写道L2上面  一下列子可以证明 一下更改6个值 和 分开更改3个值的 方法比较结果是 分开的更快
也就是说6个一起的话，会先填满4个之后还有两个等待下次填满才会输出  而分开更改的是一次性填满了四个位置，之后直接输出了

https://www.cnblogs.com/liushaodong/p/4777308.html

JUC/029_WriteCombining

乱序执行的证明：JVM/jmm/Disorder.java

## 如何保证特定情况下不乱序

硬件内存屏障 X86

>  sfence:  store| 在sfence指令前的写操作当必须在sfence指令后的写操作前完成。
>  lfence：load | 在lfence指令前的读操作当必须在lfence指令后的读操作前完成。
>  mfence：modify/mix | 在mfence指令前的读写操作当必须在mfence指令后的读写操作前完成。

> 原子指令，如x86上的”lock …” 指令是一个Full Barrier，执行时会锁住内存子系统来确保执行顺序，甚至跨多个CPU。Software Locks通常使用了内存屏障或原子指令来实现变量可见性和保持程序顺序

JVM级别如何规范（JSR133）
   jvm级别的有序性，并不是一定依赖于硬件内存屏障，它也有可能是依赖于硬件界别的使用的lock指令 comxchg
> LoadLoad屏障：
>   	对于这样的语句Load1; LoadLoad; Load2， 
>
>  	在Load2及后续读取操作要读取的数据被访问前，保证Load1要读取的数据被读取完毕。
>
> StoreStore屏障：
>
>  	对于这样的语句Store1; StoreStore; Store2，
>
>  	在Store2及后续写入操作执行前，保证Store1的写入操作对其它处理器可见。
>
> LoadStore屏障：
>
>  	对于这样的语句Load1; LoadStore; Store2，
>
>  	在Store2及后续写入操作被刷出前，保证Load1要读取的数据被读取完毕。
>
> StoreLoad屏障：
> 	对于这样的语句Store1; StoreLoad; Load2，
>
> ​	 在Load2及后续所有读取操作执行前，保证Store1的写入对所有处理器可见。

volatile的实现细节

1. 字节码层面
   ACC_VOLATILE

2. JVM层面
   volatile内存区的读写 都加屏障

   > StoreStoreBarrier
   >
   > volatile 写操作
   >
   > StoreLoadBarrier

   > LoadLoadBarrier
   >
   > volatile 读操作
   >
   > LoadStoreBarrier

3. OS和硬件层面
   https://blog.csdn.net/qq_26222859/article/details/52235930
   hsdis - HotSpot Dis Assembler
   windows lock 指令实现

synchronized实现细节

1. 字节码层面
   ACC_SYNCHRONIZED
   monitorenter monitorexit
2. JVM层面
   C C++ 调用了操作系统提供的同步机制
3. OS和硬件层面
   X86 : lock cmpxchg / xxx
   [https](https://blog.csdn.net/21aspnet/article/details/88571740)[://blog.csdn.net/21aspnet/article/details/](https://blog.csdn.net/21aspnet/article/details/88571740)[88571740](https://blog.csdn.net/21aspnet/article/details/88571740)
   
   # java 8大院子操作
   lock 主内存 表示变量为线程独占
   unlock 主内存 解锁线程独占变量
    read 主内存 读取内容到工作内存
    load 工作内存 read后的值放入线程本地变量副本
    use 工作内存 传值给执行引擎
    assign 工作内存 执行引擎结果赋值给线程本地变量
    store 工作内存 存值到主内存给write备用
    write 主内存 写变量值
    # happens-before原则  jvm重排序必须遵守的规则
    程序次序规则
    管程规定规则
    线程启动规则
    线程终止规则
    线程中断原则
    对象结束原则
    as if serial  不管如何重排序，单线程执行结果不会改变
   
   ------------------------------------------------------------------------------------------------------------------
   03_02_JavaAgent_AboutObject.md
   # 使用JavaAgent测试Object的大小
   ## 对象创建过程
      1.class loading
      2. class linking --三步 verfication--preparation resolution
      3.class initializing
      4.申请对象内存
      5.成员变量赋默认值
      6.调用构造方法《init》
         1.成员变量顺序赋值初始值
         2.执行构造方法语句

作者：马士兵 http://www.mashibing.com

## 对象大小（64位机）对象的内存布局
### markword 64位
  锁定标志位 2 bit 分代年龄 4 bit 是否偏向锁 1bit  对象的hashcode 23 bit + 2bit =25 bit 
  他是根据你不同的状态来分配你的64位 8字节的
  无锁态  对象的hashcode  分代年龄  是否偏向锁 锁标志位
  轻量级锁  指向栈中锁记录的指针  锁标志位
  重量级锁  指向互斥量（重量级锁）的指针 锁标志位
  GC标记   空                 锁标志位
  偏向锁  线程id EPoch 2位  分代年龄  是否偏向锁  锁标志位
  为什么GC年龄默认是15？因为对象头中的分代年龄就是4位，最大就是15！
  ## IdentityHashCode的问题

回答白马非马的问题：

当一个对象计算过identityHashCode之后，不能进入偏向锁状态 因为这个hashcode值以及把偏向锁中存放线程id和epoch的位置以及占了，所以进入不了偏向锁

### 观察虚拟机配置

java -XX:+PrintCommandLineFlags -version
对象在内存中存储布局？ 答案就是下面两种
### 普通对象

1. 对象头：markword  8
2. ClassPointer指针：-XX:+UseCompressedClassPointers 为4字节 不开启为8字节
3. 实例数据
   1. 引用类型：-XX:+UseCompressedOops 为4字节 不开启为8字节 
      Oops Ordinary Object Pointers 普通对象指针
4. Padding对齐，8的倍数

### 数组对象

1. 对象头：markword 8
2. ClassPointer指针同上
3. 数组长度：4字节
4. 数组数据
5. 对齐 8的倍数

## 实验

1. 新建项目ObjectSize （1.8）

2. 创建文件ObjectSizeAgent

   ```java
   package com.mashibing.jvm.agent;
   
   import java.lang.instrument.Instrumentation;
   
   public class ObjectSizeAgent {
       private static Instrumentation inst;
   
       public static void premain(String agentArgs, Instrumentation _inst) {
           inst = _inst;
       }
   
       public static long sizeOf(Object o) {
           return inst.getObjectSize(o);
       }
   }
   ```

3. src目录下创建META-INF/MANIFEST.MF

   ```java
   Manifest-Version: 1.0
   Created-By: mashibing.com
   Premain-Class: com.mashibing.jvm.agent.ObjectSizeAgent
   ```

   注意Premain-Class这行必须是新的一行（回车 + 换行），确认idea不能有任何错误提示

4. 打包jar文件

5. 在需要使用该Agent Jar的项目中引入该Jar包
   project structure - project settings - library 添加该jar包

6. 运行时需要该Agent Jar的类，加入参数：

   ```java
   -javaagent:C:\work\ijprojects\ObjectSize\out\artifacts\ObjectSize_jar\ObjectSize.jar
   ```

7. 如何使用该类：

   ```java
   ​```java
      package com.mashibing.jvm.c3_jmm;
      
      import com.mashibing.jvm.agent.ObjectSizeAgent;
      
      public class T03_SizeOfAnObject {
          public static void main(String[] args) {
              System.out.println(ObjectSizeAgent.sizeOf(new Object()));
              System.out.println(ObjectSizeAgent.sizeOf(new int[] {}));
              System.out.println(ObjectSizeAgent.sizeOf(new P()));
          }
      
          private static class P {
                              //8 _markword
                              //4 _oop指针
              int id;         //4
              String name;    //4
              int age;        //4
      
              byte b1;        //1
              byte b2;        //1
      
              Object o;       //4
              byte b3;        //1
      
          }
      }
   ```

## Hotspot开启内存压缩的规则（64位机）

1. 4G以下，直接砍掉高32位
2. 4G - 32G，默认开启内存压缩 ClassPointers Oops
3. 32G，压缩无效，使用64位
   内存并不是越大越好（^-^）

## IdentityHashCode的问题

回答白马非马的问题：

当一个对象计算过identityHashCode之后，不能进入偏向锁状态

https://cloud.tencent.com/developer/article/1480590
 https://cloud.tencent.com/developer/article/1484167

https://cloud.tencent.com/developer/article/1485795

https://cloud.tencent.com/developer/article/1482500

## 对象定位

•https://blog.csdn.net/clover_lily/article/details/80095580
深入理解java虚拟机 周老师的书上有讲
T t =new T（）； t如何找到这个new对象的
1. 句柄池   一端指向t，另一端指向new对象  cms三色标记中使用的就是句柄池效率更高
2. 直接指针 t直接new 对象的地址   hotspot用的就是直接指针

## 对象如何分配
一个对象先new的时候--尝试往栈上分配，栈上能够分配就在栈上分配，栈一弹出，对象就没有了----如果栈上分配不下而且对象很大，直接分配到堆内存老年代----如果对象不大，线程本地分配，能分配就分配，分配不了就找伊甸区----gc过程，年龄到了就到老年代，年龄没到就gc来gc去
-----------------------------------------------------------------------------------------------------
04_JavaRuntimeDataArea_InstructionSet.md
# Runtime Data Area and Instruction Set

jvms 2.4 2.5

## 指令集分类

1. 基于寄存器的指令集
2. 基于栈的指令集
   Hotspot中的Local Variable Table = JVM中的寄存器

## Runtime Data Area

### PC 程序计数器 program counter

> 存放指令位置
>
> 虚拟机的运行，类似于这样的循环：
>
> while( not end ) {
>
> ​	取PC中的位置，找到对应位置的指令；
>
> ​	执行该指令；
>
> ​	PC ++;
>
> }

### JVM Stack
每个线程都有一个自己jvm stack
1. Frame - $\color{#FF3030}{red}$每个方法对应一个栈帧
   1. Local Variable Table  局部变量，本地变量
   2. Operand Stack 操作数栈
      对于long的处理（store and load），多数虚拟机的实现都是原子的
      jls 17.7，没必要加volatile
   3. Dynamic Linking 动态链接
      一个线程有自己的线程栈，每个线程栈里面装着自己的栈帧，每个栈针里面有自己的操作数栈和 动态链接 dynamic linking，
      指到我们运行时常量池，也就是我们class文件里的常量池的链接，找到这个符号链接，看它有没有解析，如果解析就直接用，没有解析就动态解析
      也就是A方法里面调用了B方法，那么这个B方法是不是要从常量池中获取，那个这个指向的操作或者链接就是 动态链接
       https://blog.csdn.net/qq_41813060/article/details/88379473 
      jvms 2.6.3
   4. return address 返回地址
      a() -> b()，方法a调用了方法b, b方法的返回值放在什么地方，或者方法执行完了应该回答哪里执行，这个就是返回地址

### Heap
   jvm有一个堆，在线程中共享
### Method Area
方法去是被所有线程共享的
   每一个class里面的结构放在 methad area里面
   方法区是一个逻辑概念，它的具体实现是一下两个：
1. Perm Space (<1.8)
   字符串常量位于PermSpace
   FGC不会清理
   大小启动的时候指定，不能变
2. Meta Space (>=1.8)
   字符串常量位于堆
   会触发FGC清理
   不设定的话，最大就是物理内存

### Runtime Constant Pool
   常量值内容在运行的时候是在这个里面的
### Native Method Stack 等同于java方法自己的栈，我们也无法调优一般接触不到

### Direct Memory 直接内存 用户空间可以直接访问内核空间的内存 1.4版本之后就出现了nio，提高效率

### 总结：每一个线程有自己的程序计数器Pc JVM Stack Native Method Stack ，他们共享的区域是 堆heap 和 method area （perm space/meta space） 为什么每个线程都有自己PC，因为会有线程切换，需要保留现场下次切换回来的时候知道自己应该继续执行哪里
> JVM可以直接访问的内核空间的内存 (OS 管理的内存)
>
> NIO ， 提高效率，实现zero copy

思考：
### i++和++i的区别在于 i++是先iload后iinc，而++是先iinc后iload的，所以i++返回的i在栈中的值，++i返回的加一之后压回栈的值
> 如何证明1.7字符串常量位于Perm，而1.8位于Heap？
>
> 提示：结合GC， 一直创建字符串常量，观察堆，和Metaspace

### 补充：
基于栈的指令集 相对简单只有压栈，出栈
基于寄存器的指令集  相对复杂，但是运行快   hotspot的local variable table 本地局部变量池

## 常用指令
bipush 压栈，并把byte转换成int value
istore_<n> 比如istore_1 就是把栈上面弹出一个，放到局部变量表上的1号上的位置
iload_<n> 比如iload_1 就是把局部变量表上的1号上的位置的值进行压栈
aload_0 把this进行压栈
store

load

pop

mul

sub

invoke

1. InvokeStatic
2. InvokeVirtual
3. InvokeInterface
4. InovkeSpecial
   可以直接定位，不需要多态的方法
   private 方法 ， 构造方法
5. InvokeDynamic
   JVM最难的指令
   lambda表达式或者反射或者其他动态语言scala kotlin，或者CGLib ASM，动态产生的class，会用到的指令
   
