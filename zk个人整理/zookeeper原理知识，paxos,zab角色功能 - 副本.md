![](D:\马士兵架构\myProject\zk个人整理\images\image-20200612173249778.png)

1.Paxos 定向去百度搜索  paxos site douban.com

Paxos，它是一个基于消息传递的一致性算法  Paxos有一个前提：没有拜占庭将军问题 就是说Paxos只有在一个可信的计算环境中才能成立，这个环境是不会被入侵所破坏的

2.ZAB

3.watch

4.API  不怕写zk client

5.callback----》reactive 响应式编程

更充分的压栈OS，HW资源，性能

zookeeper分布式协调  扩展性 可靠性 顺序性（时序性）快速！！！

**扩展性**  -----框架架构---------角色---------------leader follower 主从  observer

​			-------读写分离 observer放大查询能力-------只有follwer才能选举

​				--------zoo.cfg server.1=node01:2888:3888等等 如果你要跳转为observer就在后面加上server.2=node02:2888:3888:Observer

**可靠性**-------攘其外（服务可用）必先安其内（快速选主）

​			-------快速恢复Leader

​			--------数据 可靠 可用 一致性-------攘其外  一致性？最终一致性是如何实现的？ 过程中，节点是否堆外提供服务

​                                                           -------分布式----https://www.douban.com/note/208430424/

​																			-----ZAB zk 原子广播协议  作用在可用状态 有Leader时

原子 成功，失败，没有中间状态   广播---分布式多节点的，并不代表全部知道！！！  队列：先进先出FIFO 顺序性   zk的数据状态在内存 用磁盘保存日志

![image-20200615162716327](D:\马士兵架构\myProject\zk个人整理\images\image-20200615162716327.png)

开始场景 node01 m1 z0 node02 m2 z0 node03 m3 z0 node04 m4 z0

重启场景 node01 m1 z8 node02 m2 z8 node03 m3 z7  leader---node04 m4 z8

加入node04挂掉了，这个时候node01 node02还没有发现node04挂掉了，node03发现了，并且它的zxid还是低版本的，这个时候他会拿着它的事务id去node01，node02开始投票，这个时候node01，node02会否定node03的投票，并返回正确的zxid 8 回去，然后node01 node02分别投自己一片，发给node03和另一个节点，**最后根据mid和zxid的不同，node03会投node02，node01也会投node02**

![image-20200615162808720](D:\马士兵架构\myProject\zk个人整理\images\image-20200615162808720.png)

为什么有redis还要有zookeeper？

1. 在统一视图 目录树结构的情况下加入了 watch 监控就类似于心跳验证 （时效性没有watch及时） 一个客户端创建 临时节点 session+E node 这个时候在过期之后node就会消失

![image-20200615162827826](D:\马士兵架构\myProject\zk个人整理\images\image-20200615162827826.png)

//zk是有session概念的，没有连接池的概念
   //watch:观察，回调
   //watch的注册只发生在 读类型调用，get，exites。。。
  //第一类：new zk 时候，传入的watch，这个watch，session级别的，跟path 、node没有关系。











































