

# KafKa第一节

一个分区是不能分给两个消费者的，但是两个分区是可以共同给一个消费者

![image-20210125201349671](D:\GitHub\myProject\KafKa\images\image-20210125201349671.png)

![image-20210125201433845](D:\GitHub\myProject\KafKa\images\image-20210125201433845.png)

![image-20210125201915157](D:\GitHub\myProject\KafKa\images\image-20210125201915157.png)

![image-20210125201938054](D:\GitHub\myProject\KafKa\images\image-20210125201938054.png)

自己维护offset默认是50个partition

![image-20210125202005673](D:\GitHub\myProject\KafKa\images\image-20210125202005673.png)



# KafKa第二节

#### 这里需要安装zk和kafka，自己根据视频自己总结安装记录

![image-20210125221920508](D:\GitHub\myProject\KafKa\images\image-20210125221920508.png)



![image-20201213114957385](D:\GitHub\myProject\KafKa\images\image-20201213114957385.png)

![image-20201213115801417](D:\GitHub\myProject\KafKa\images\image-20201213115801417.png)



![image-20201213120007309](D:\GitHub\myProject\KafKa\images\image-20201213120007309.png)

![image-20210125230238352](D:\GitHub\myProject\KafKa\images\image-20210125230238352.png)

#### 怎么解决多线程？

![image-20201213184655747](D:\GitHub\myProject\KafKa\images\image-20201213184655747.png)

#### 提交方式变化

![image-20201213184747949](D:\GitHub\myProject\KafKa\images\image-20201213184747949.png)







![image-20201213184833577](D:\GitHub\myProject\KafKa\images\image-20201213184833577.png)

![image-20201213184907008](D:\GitHub\myProject\KafKa\images\image-20201213184907008.png)

# KafKa第三节

代码写，生产者与消费者



# KafKa第四节

![image-20210126215030946](D:\GitHub\myProject\KafKa\images\image-20210126215030946.png)

![image-20210126215113078](D:\GitHub\myProject\KafKa\images\image-20210126215113078.png)

### 分区可靠性，要解决一个问题

![image-20210126220615916](D:\GitHub\myProject\KafKa\images\image-20210126220615916.png)

#### ack为-1的时候，处于ISR的broker中数据必须是一致的

![image-20210126220438309](D:\GitHub\myProject\KafKa\images\image-20210126220438309.png)

#### ack为1的时候，处于ISR的broker中数据是可以不一致的

![image-20210126221811405](D:\GitHub\myProject\KafKa\images\image-20210126221811405.png)

![image-20210126222501515](D:\GitHub\myProject\KafKa\images\image-20210126222501515.png)

pagecache就是作用在当前面橙色数据在前面消费的时候，紫色数据就已经开始缓存pagecache的数据了，当橙色消费到紫色数据的时候，就会直接使用pagecache数据经过sendfile来返回数据了

![image-20210126224007598](D:\GitHub\myProject\KafKa\images\image-20210126224007598.png)

