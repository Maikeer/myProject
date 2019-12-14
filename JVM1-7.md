
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
每个线程一个PC

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
1. Frame **每个方法对应一个栈帧** 
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
方法区是被所有线程共享的
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
DCL为什么要使用volital，因为这个地方有可能会指令重排序
## 常用指令
**非static方法中的局部变量表上会在0号位置上放入一个this**
sipush 默认就是short也会转换成int类型  比如 int i=200;因为200超过了byte的最大值127
bipush 压栈，并把byte转换成int value 比如 int i=100
istore_<n> 比如istore_1 就是把栈上面弹出一个，放到局部变量表上的1号上的位置
iload_<n> 比如iload_1 就是把局部变量表上的1号上的位置的值进行压栈
aload_0 把this进行压栈
new 分配内存--赋值默认值
dup copy一个this地址放入栈中
store
iadd 把栈上的两个值进行相加，然后相加值再压入栈中
load
iconst_1 从常量池中1这个数值压栈
if icmpne 取弹出栈中的两个值进行比较，如果不相等，就跳转
pop 从栈中弹出，因为一个方法1如果有返回值的话，会把返回值压入调用这个方法1的方法2中栈顶中
isub 弹出栈中两个值并计算差值，再压入栈中
imul 去栈中两个值进行相加计算并压栈

sub

invoke

1. InvokeStatic 调用的静态方法时，使用的是这个
2. InvokeVirtual 弹出栈中的对象调用非静态方法，自带多态的，final修饰的非静态方法也是 invokeVitual
3. InvokeInterface
4. InovkeSpecial
   可以直接定位，不需要多态的方法
   private 方法 ， 构造方法（过程中，会给局部变量赋初始值）
5. InvokeDynamic
   JVM最难的指令
   lambda表达式或者反射或者其他动态语言scala kotlin，或者CGLib ASM，动态产生的class，会用到的指令
   C::n这个代表是一个方法，在java 中会在内部类中创建一个内部类，比如 I i=C::n； 他在class中就会调用 invokeDynamic来
   for(;;){
    I i=C::n；
   }
   以上这个代码中会产生很多的class，会放在方法区中，methodArea中， 在1.8之前 fullgc是不会回收的，OOM为outofmemory的简称,称之为内存溢出
   
   # GC和GC Tuning

作者：马士兵教育 http://mashibing.com

### GC的基础知识

#### 1.什么是垃圾

> C语言申请内存：malloc free
>
> C++： new delete
>
> c/C++ 手动回收内存
>
> Java: new ？
>
> 自动内存回收，编程上简单，系统不容易出错，手动释放内存，容易出两种类型的问题：
>
> 1. 忘记回收
> 2. 多次回收

没有任何引用指向的一个对象或者多个对象（循环引用）

#### 2.如何定位垃圾

1. 引用计数
2. 根可达算法

#### 3.常见的垃圾回收算法

1. 标记清除 - 位置不连续 产生碎片 效率偏低（两遍扫描）
2. 拷贝算法 - 没有碎片，浪费空间
3. 标记压缩 - 没有碎片，效率偏低（两遍扫描，指针需要调整）

#### 4.JVM内存分代模型（用于分代垃圾回收算法）

1. 部分垃圾回收器使用的模型

   > 除Epsilon ZGC Shenandoah之外的GC都是使用逻辑分代模型
   >
   > G1是逻辑分代，物理不分代
   >
   > 除此之外不仅逻辑分代，而且物理分代

2. 新生代 + 老年代 + 永久代（1.7）/ 元数据区(1.8) Metaspace
   1. 永久代 元数据 - Class
   2. 永久代必须指定大小限制 ，元数据可以设置，也可以不设置，无上限（受限于物理内存）
   3. 字符串常量 1.7 - 永久代，1.8 - 堆
   4. MethodArea逻辑概念 - 永久代、元数据
   
3. 新生代 = Eden + 2个suvivor区 
   1. YGC回收之后，大多数的对象会被回收，活着的进入s0
   2. 再次YGC，活着的对象eden + s0 -> s1
   3. 再次YGC，eden + s1 -> s0
   4. 年龄足够 -> 老年代 （15 CMS 6）
   5. s区装不下 -> 老年代
   
4. 老年代
   1. 顽固分子
   2. 老年代满了FGC Full GC
   
5. GC Tuning (Generation)
   1. 尽量减少FGC
   2. MinorGC = YGC
   3. MajorGC = FGC
   
6. 对象分配过程图
   ![](对象分配过程详解.png)

7. 动态年龄：（不重要）
   https://www.jianshu.com/p/989d3b06a49d

8. 分配担保：（不重要）
   YGC期间 survivor区空间不够了 空间担保直接进入老年代

#### 5.常见的垃圾回收器

![常用垃圾回收器](常用垃圾回收器.png)

1. JDK诞生 Serial追随 提高效率，诞生了PS，为了配合CMS，诞生了PN，CMS是1.4版本后期引入，CMS是里程碑式的GC，它开启了并发回收的过程，但是CMS毛病较多，因此目前任何一个JDK版本默认是CMS
   并发垃圾回收是因为无法忍受STW
2. Serial 年轻代 串行回收
3. PS 年轻代 并行回收
4. ParNew 年轻代 配合CMS的并行回收
5. SerialOld 
6. ParallelOld
7. ConcurrentMarkSweep 老年代 并发的， 垃圾回收和应用程序同时运行，降低STW的时间(200ms)
   CMS问题比较多，所以现在没有一个版本默认是CMS，只能手工指定
   CMS既然是MarkSweep，就一定会有碎片化的问题，碎片到达一定程度，CMS的老年代分配对象分配不下的时候，使用SerialOld 进行老年代回收
   想象一下：
   PS + PO -> 加内存 换垃圾回收器 -> PN + CMS + SerialOld（几个小时 - 几天的STW）
   几十个G的内存，单线程回收 -> G1 + FGC 几十个G -> 上T内存的服务器 ZGC
   算法：三色标记 + Incremental Update
8. G1(10ms)
   算法：三色标记 + SATB
9. ZGC (1ms) PK C++
   算法：ColoredPointers + 写屏障？
10. Shenandoah
    算法：ColoredPointers + 读屏障?
11. Eplison
12. PS 和 PN区别的延伸阅读：
    ▪[https://docs.oracle.com/en/java/javase/13/gctuning/ergonomics.html#GUID-3D0BB91E-9BFF-4EBB-B523-14493A860E73](https://docs.oracle.com/en/java/javase/13/gctuning/ergonomics.html)
13. 垃圾收集器跟内存大小的关系
    1. Serial 几十兆
    2. PS 上百兆 - 几个G
    3. CMS - 20G
    4. G1 - 上百G
    5. ZGC - 4T

1.8默认的垃圾回收：PS + ParallelOld

### JVM调优第一步，了解JVM常用命令行参数

* JVM的命令行参数参考：https://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html

* HotSpot参数分类

  > 标准： - 开头，所有的HotSpot都支持
  >
  > 非标准：-X 开头，特定版本HotSpot支持特定命令
  >
  > 不稳定：-XX 开头，下个版本可能取消

  java -version

  java -X

  

  试验用程序：

  ```java
  import java.util.List;
  import java.util.LinkedList;
  
  public class HelloGC {
    public static void main(String[] args) {
      System.out.println("HelloGC!");
      List list = new LinkedList();
      for(;;) {
        byte[] b = new byte[1024*1024];
        list.add(b);
      }
    }
  }
  ```

  区分概念：内存泄漏 memory leak 内存溢出 out of memory

  1. java -XX:+PrintCommandLineFlags HelloGC
  2. java -Xmn10M -Xms40M -Xmx60M -XX:+PrintCommandLineFlags -XX:PrintGC  HelloGC
  3. java -XX:+UseConcMarkSweepGC -XX:+PrintCommandLineFlags HelloGC
  4. java -XX:+PrintFlagsInitial 默认参数值
  5. java -XX:+PrintFlagsFinal 最终参数值
  6. java -XX:+PrintFlagsFinal | grep xxx 找到对应的参数
  7. java -XX:+PrintFlagsFinal -version |grep GC

### PS GC日志详解

每种垃圾回收器的日志格式是不同的！

PS日志格式

![GC日志详解](./GC日志详解.png)

heap dump部分：

```java
eden space 5632K, 94% used [0x00000000ff980000,0x00000000ffeb3e28,0x00000000fff00000)
                            后面的内存地址指的是，起始地址，使用空间结束地址，整体空间结束地址
```

### 常见垃圾回收器组合参数设定：(1.8)

* -XX:+UseSerialGC = Serial New (DefNew) + Serial Old
  * 小型程序。默认情况下不会是这种选项，HotSpot会根据计算及配置和JDK版本自动选择收集器
* -XX:+UseParNewGC = ParNew + SerialOld
  * 这个组合已经很少用（在某些版本中已经废弃）
  * https://stackoverflow.com/questions/34962257/why-remove-support-for-parnewserialold-anddefnewcms-in-the-future
* UseConc<font color=red>(urrent)</font>MarkSweepGC = ParNew + CMS + Serial Old
* UseParallelGC = Parallel Scavenge + Parallel Old (1.8默认) 【PS + SerialOld】
* UseParallelOldGC = Parallel Scavenge + Parallel Old
* UseG1GC = G1
* Linux中没找到默认GC的查看方法，而windows中会打印UseParallelGC 
  * java +XX:+PrintCommandLineFlags -version
  * 通过GC的日志来分辨

* Linux下1.8版本默认的垃圾回收器到底是什么？

  * 1.8.0_181 默认（看不出来）Copy MarkCompact
  * 1.8.0_222 默认 PS + PO
 所谓调优，首先确定，追求啥？吞吐量优先，还是响应时间优先？还是在满足一定的响应时间的情况下，要求达到多打的吞吐量。。。
     问题：科学计算 吞吐量 数据挖掘 吞吐量 （throput）吞吐量优先的一般：PS+PO
     如果是响应时间优先 网站 GUI API  1.8 G1尽量选
### 什么是调优？

1. 根据需求进行JVM规划和预调优
2. 优化运行JVM运行环境 （慢，卡顿）
3. 解决JVM运行过程中出现的各种问题

### 调优，从规划开始

* 调优，从业务场景开始，没有业务场景的调优都是耍流氓
  压测
  
* 无监控，不调优

* 步骤：
  1. 熟悉业务场景（没有最好的垃圾回收器，只有最合适的垃圾回收器）
     1. 响应时间、停顿时间 [CMS G1 ZGC] （需要给用户作响应）stw越短，相应时间越好
     2. 吞吐量 = 用户时间 /( 用户时间 + GC时间) [PS]
    
  2. 选择回收器组合
  3. 计算内存需求（经验值 1.5G 16G）
  4. 设定年代大小、升级年龄
  5. 设定日志参数
     1. -Xloggc:/opt/xxx/logs/xxx-xxx-gc-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=20M -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCCause
     2. 或者每天产生一个日志文件
  6. 观察日志情况
  
* 案例1：垂直电商，最高每日百万订单，处理订单系统需要什么样的服务器配置？

  > 这个问题比较业余，因为很多不同的服务器配置都能支撑(1.5G 16G)
  >
  > 1小时360000集中时间段， 100个订单/秒
  >
  > 经验值，
  >
  > 专业一点儿问法：要求响应时间100ms

* 案例2：12306遭遇春节大规模抢票应该如何支撑？

  > 12306应该是中国并发量最大的秒杀网站：
  >
  > 号称并发量100W最高
  >
  > CDN -> LVS -> NGINX -> 业务系统 -> 每台机器1W并发 100台机器
  >
  > 普通电商订单 -> 下单 ->订单系统（IO）减库存 ->等待用户付款
  >
  > 12306的一种可能的模型： 下单 -> 减库存 和 订单(redis kafka) 同时异步进行 ->等付款
  >
  > 减库存最后还会把压力压到一台服务器
  >
  > 可以做分布式本地库存 + 单独服务器做库存均衡
  ### 所谓的大流量的处理方法就是 分而治之

* 怎么得到一个事务会消耗多少内存？

  > 1. 弄台机器，看能承受多少TPS？是不是达到目标？扩容或调优，让它达到
  >
  > 2. 用压测来确定

### 优化环境

1. 有一个50万PV的资料类网站 类似于百度文档（从磁盘提取文档到内存）原服务器32位，1.5G
   的堆，用户反馈网站比较缓慢，因此公司决定升级，新的服务器为64位，16G
   的堆内存，结果用户反馈卡顿十分严重，反而比以前效率更低了
   为什么？
      为什么原网站慢？很多用户浏览数据，很多数据load到内存，内存不足，频繁gc，STW长，响应时间变慢
      为什么更卡顿？ 内存越大，FGC时间更长
      咋办？ PS换成 parnew+cms 或者 g1
   如何优化？
2. 系统CPU经常100%，如何调优？ 面试高频
cpu100%那么一定有线程再占有系统资源，
1.找出那个进程CPU高 top
2.该进程中的那个线程cpu高 top -HP
3,。导出该线程的堆栈 jstack
4查找那个方法 栈帧 消耗时间 jstack
3.系统内存飙高，如何查问题？ 面试高频
1.导出堆内存 jmap
2.分析 jhat jvisualvm mat jprofiler工具

4如何监控jvm
1.jstat jviualvm jprofiler arthas


### 解决JVM运行中的问题

#### 一个案例理解常用工具

1. 测试代码：

   ```java
   package com.mashibing.jvm.gc;
   
   import java.math.BigDecimal;
   import java.util.ArrayList;
   import java.util.Date;
   import java.util.List;
   import java.util.concurrent.ScheduledThreadPoolExecutor;
   import java.util.concurrent.ThreadPoolExecutor;
   import java.util.concurrent.TimeUnit;
   
   /**
    * 从数据库中读取信用数据，套用模型，并把结果进行记录和传输
    */
   
   public class T15_FullGC_Problem01 {
   
       private static class CardInfo {
           BigDecimal price = new BigDecimal(0.0);
           String name = "张三";
           int age = 5;
           Date birthdate = new Date();
   
           public void m() {}
       }
   
       private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(50,
               new ThreadPoolExecutor.DiscardOldestPolicy());
   
       public static void main(String[] args) throws Exception {
           executor.setMaximumPoolSize(50);
   
           for (;;){
               modelFit();
               Thread.sleep(100);
           }
       }
   
       private static void modelFit(){
           List<CardInfo> taskList = getAllCardInfo();
           taskList.forEach(info -> {
               // do something
               executor.scheduleWithFixedDelay(() -> {
                   //do sth with info
                   info.m();
   
               }, 2, 3, TimeUnit.SECONDS);
           });
       }
   
       private static List<CardInfo> getAllCardInfo(){
           List<CardInfo> taskList = new ArrayList<>();
   
           for (int i = 0; i < 100; i++) {
               CardInfo ci = new CardInfo();
               taskList.add(ci);
           }
   
           return taskList;
       }
   }
   
   ```

2. java -Xms200M -Xmx200M com.mashibing.jvm.gc.T15_FullGC_Problem01

3. top命令观察到问题：内存不断增长 CPU占用率居高不下

4. jps定位具体java进程

5. jinfo pid 

6. jstat -gc 动态观察gc情况 / 阅读GC日志发现频繁GC / arthas观察 / jconsole
   jstat -gc 4655 500 : 每个500个毫秒打印GC的情况

7. jmap - histo 4655 | head -20，查找有多少对象产生

8. jmap -dump:format=b,file=xxx pid / jmap -histo

9. java -Xms20M -Xmx20M -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError com.mashibing.jvm.gc.T15_FullGC_Problem01

10. 使用MAT / jhat进行dump文件分析
     https://www.cnblogs.com/baihuitestsoftware/articles/6406271.html 

11. 找到代码的问题

#### jconsole远程连接

1. 程序启动加入参数：

   > ```shell
   > java -Djava.rmi.server.hostname=192.168.17.11 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=11111 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false XXX
   > ```

2. 如果遭遇 Local host name unknown：XXX的错误，修改/etc/hosts文件，把XXX加入进去

   > ```java
   > 192.168.17.11 basic localhost localhost.localdomain localhost4 localhost4.localdomain4
   > ::1         localhost localhost.localdomain localhost6 localhost6.localdomain6
   > ```

3. 关闭linux防火墙（实战中应该打开对应端口）

   > ```shell
   > service iptables stop
   > chkconfig iptables off #永久关闭
   > ```

4. windows上打开 jconsole远程连接 192.168.17.11:11111

#### jvisualvm远程连接

 https://www.cnblogs.com/liugh/p/7620336.html （简单做法）

#### jprofiler (收费)

#### arthas在线排查工具

* 为什么需要在线排查？
   在生产上我们经常会碰到一些不好排查的问题，例如线程安全问题，用最简单的threaddump或者heapdump不好查到问题原因。为了排查这些问题，有时我们会临时加一些日志，比如在一些关键的函数里打印出入参，然后重新打包发布，如果打了日志还是没找到问题，继续加日志，重新打包发布。对于上线流程复杂而且审核比较严的公司，从改代码到上线需要层层的流转，会大大影响问题排查的进度。 

### CMS

#### CMS的问题

1. Memory Fragmentation

   > -XX:+UseCMSCompactAtFullCollection
   > -XX:CMSFullGCsBeforeCompaction 默认为0 指的是经过多少次FGC才进行压缩

2. Floating Garbage

   > Concurrent Mode Failure
   > 产生：if the concurrent collector is unable to finish reclaiming the unreachable objects before the tenured generation fills up, or if an allocation cannot be satisfiedwith the available free space blocks in the tenured generation, then theapplication is paused and the collection is completed with all the applicationthreads stopped
   >
   > 解决方案：降低触发CMS的阈值
   >
   > PromotionFailed
   >
   > 解决方案类似，保持老年代有足够的空间
   >
   > –XX:CMSInitiatingOccupancyFraction 92% 可以降低这个值，让CMS保持老年代足够的空间

### 案例汇总

1. 硬件升级系统反而卡顿的问题（见上）

2. 线程池不当运用产生OOM问题（见上）

3. smile jira问题

4. tomcat http-header-size过大问题

5. lambda表达式导致方法区溢出问题
   LambdaGC.java     -XX:MaxMetaspaceSize=9M -XX:+PrintGCDetails

   ```java
   "C:\Program Files\Java\jdk1.8.0_181\bin\java.exe" -XX:MaxMetaspaceSize=9M -XX:+PrintGCDetails "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2019.1\lib\idea_rt.jar=49316:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2019.1\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files\Java\jdk1.8.0_181\jre\lib\charsets.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\deploy.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\access-bridge-64.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\cldrdata.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\dnsns.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\jaccess.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\jfxrt.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\localedata.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\nashorn.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\sunec.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\sunjce_provider.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\sunmscapi.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\sunpkcs11.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\ext\zipfs.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\javaws.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\jce.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\jfr.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\jfxswt.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\jsse.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\management-agent.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\plugin.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\resources.jar;C:\Program Files\Java\jdk1.8.0_181\jre\lib\rt.jar;C:\work\ijprojects\JVM\out\production\JVM;C:\work\ijprojects\ObjectSize\out\artifacts\ObjectSize_jar\ObjectSize.jar" com.mashibing.jvm.gc.LambdaGC
   [GC (Metadata GC Threshold) [PSYoungGen: 11341K->1880K(38400K)] 11341K->1888K(125952K), 0.0022190 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
   [Full GC (Metadata GC Threshold) [PSYoungGen: 1880K->0K(38400K)] [ParOldGen: 8K->1777K(35328K)] 1888K->1777K(73728K), [Metaspace: 8164K->8164K(1056768K)], 0.0100681 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
   [GC (Last ditch collection) [PSYoungGen: 0K->0K(38400K)] 1777K->1777K(73728K), 0.0005698 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
   [Full GC (Last ditch collection) [PSYoungGen: 0K->0K(38400K)] [ParOldGen: 1777K->1629K(67584K)] 1777K->1629K(105984K), [Metaspace: 8164K->8156K(1056768K)], 0.0124299 secs] [Times: user=0.06 sys=0.00, real=0.01 secs] 
   java.lang.reflect.InvocationTargetException
   	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
   	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
   	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
   	at java.lang.reflect.Method.invoke(Method.java:498)
   	at sun.instrument.InstrumentationImpl.loadClassAndStartAgent(InstrumentationImpl.java:388)
   	at sun.instrument.InstrumentationImpl.loadClassAndCallAgentmain(InstrumentationImpl.java:411)
   Caused by: java.lang.OutOfMemoryError: Compressed class space
   	at sun.misc.Unsafe.defineClass(Native Method)
   	at sun.reflect.ClassDefiner.defineClass(ClassDefiner.java:63)
   	at sun.reflect.MethodAccessorGenerator$1.run(MethodAccessorGenerator.java:399)
   	at sun.reflect.MethodAccessorGenerator$1.run(MethodAccessorGenerator.java:394)
   	at java.security.AccessController.doPrivileged(Native Method)
   	at sun.reflect.MethodAccessorGenerator.generate(MethodAccessorGenerator.java:393)
   	at sun.reflect.MethodAccessorGenerator.generateSerializationConstructor(MethodAccessorGenerator.java:112)
   	at sun.reflect.ReflectionFactory.generateConstructor(ReflectionFactory.java:398)
   	at sun.reflect.ReflectionFactory.newConstructorForSerialization(ReflectionFactory.java:360)
   	at java.io.ObjectStreamClass.getSerializableConstructor(ObjectStreamClass.java:1574)
   	at java.io.ObjectStreamClass.access$1500(ObjectStreamClass.java:79)
   	at java.io.ObjectStreamClass$3.run(ObjectStreamClass.java:519)
   	at java.io.ObjectStreamClass$3.run(ObjectStreamClass.java:494)
   	at java.security.AccessController.doPrivileged(Native Method)
   	at java.io.ObjectStreamClass.<init>(ObjectStreamClass.java:494)
   	at java.io.ObjectStreamClass.lookup(ObjectStreamClass.java:391)
   	at java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1134)
   	at java.io.ObjectOutputStream.defaultWriteFields(ObjectOutputStream.java:1548)
   	at java.io.ObjectOutputStream.writeSerialData(ObjectOutputStream.java:1509)
   	at java.io.ObjectOutputStream.writeOrdinaryObject(ObjectOutputStream.java:1432)
   	at java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1178)
   	at java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:348)
   	at javax.management.remote.rmi.RMIConnectorServer.encodeJRMPStub(RMIConnectorServer.java:727)
   	at javax.management.remote.rmi.RMIConnectorServer.encodeStub(RMIConnectorServer.java:719)
   	at javax.management.remote.rmi.RMIConnectorServer.encodeStubInAddress(RMIConnectorServer.java:690)
   	at javax.management.remote.rmi.RMIConnectorServer.start(RMIConnectorServer.java:439)
   	at sun.management.jmxremote.ConnectorBootstrap.startLocalConnectorServer(ConnectorBootstrap.java:550)
   	at sun.management.Agent.startLocalManagementAgent(Agent.java:137)
   
   ```

6. 直接内存溢出问题（少见）
   《深入理解Java虚拟机》P59，使用Unsafe分配直接内存，或者使用NIO的问题

7. 栈溢出问题
   -Xss设定太小

8. 比较一下这两段程序的异同，分析哪一个是更优的写法：

   ```java 
   Object o = null;
   for(int i=0; i<100; i++) {
       o = new Object();
   }
   ```

   ```java
   for(int i=0; i<100; i++) {
       Object o = new Object();
   }
   ```

9. 

#### 作业

1. -XX:MaxTenuringThreshold控制的是什么？
   	A: 对象升入老年代的年龄
      	B: 老年代触发FGC时的内存垃圾比例
   
2. 生产环境中，倾向于将最大堆内存和最小堆内存设置为：（为什么？）
   	A: 相同 B：不同
   
3. JDK1.8默认的垃圾回收器是：
   	A: ParNew + CMS
      	B: G1
      	C: PS + ParallelOld
      	D: 以上都不是
   
4. 什么是响应时间优先？

5. 什么是吞吐量优先？

6. ParNew和PS的区别是什么？

7. ParNew和ParallelOld的区别是什么？（年代不同，算法不同）

8. 长时间计算的场景应该选择：A：停顿时间 B: 吞吐量

9. 大规模电商网站应该选择：A：停顿时间 B: 吞吐量

10. HotSpot的垃圾收集器最常用有哪些？

11. 常见的HotSpot垃圾收集器组合有哪些？

12. JDK1.7 1.8 1.9的默认垃圾回收器是什么？如何查看？

13. 所谓调优，到底是在调什么？

14. 如果采用PS + ParrallelOld组合，怎么做才能让系统基本不产生FGC

15. 如果采用ParNew + CMS组合，怎样做才能够让系统基本不产生FGC

     1.加大JVM内存

     2.加大Young的比例

     3.提高Y-O的年龄

     4.提高S区比例

     5.避免代码内存泄漏

16. G1是否分代？G1垃圾回收器会产生FGC吗？

17. 如果G1产生FGC，你应该做什么？

     1. 扩内存
     2. 提高CPU性能（回收的快，业务逻辑产生对象的速度固定，垃圾回收越快，内存空间越大）
    
 18. 问：生产环境中能够随随便便的dump吗？
     小堆影响不大，大堆会有服务暂停或卡顿（加live可以缓解），dump前会有FGC
     
 19. 问：常见的OOM问题有哪些？
     栈 堆 MethodArea 直接内存

### 参考资料

1. [https://blogs.oracle.com/](https://blogs.oracle.com/jonthecollector/our-collectors)[jonthecollector](https://blogs.oracle.com/jonthecollector/our-collectors)[/our-collectors](https://blogs.oracle.com/jonthecollector/our-collectors)
2. https://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html
3. http://java.sun.com/javase/technologies/hotspot/vmoptions.jsp
4.  JVM调优参考文档：https://docs.oracle.com/en/java/javase/13/gctuning/introduction-garbage-collection-tuning.html#GUID-8A443184-7E07-4B71-9777-4F12947C8184 
5.  https://www.cnblogs.com/nxlhero/p/11660854.html 在线排查工具
6.  https://www.jianshu.com/p/507f7e0cc3a3 arthas常用命令
7. Arthas手册：
    1. 启动arthas java -jar arthas-boot.jar
    2. 绑定java进程
    3. dashboard命令观察系统整体情况
    4. help 查看帮助
    5. help xx 查看具体命令帮助
8. jmap命令参考： https://www.jianshu.com/p/507f7e0cc3a3 
    1. jmap -heap pid
    2. jmap -histo pid
    3. jmap -clstats pid

### ParallelGC常用参数

JVM GC 实现
   
