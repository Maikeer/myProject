# 系统IO 变化模型 第四节

###### route -n 查看路由关系  route add -host ip gw 下一条ip

###### tcpdump -nn -i ens33 port 9090

route add -net 192.168.5.16 netmask 255.255.255.0 gw 192.168.121.1

route add -host 192.168.5.16 gw 192.168.121.1

其实这个192.168.121.2这个网关是持有wm-nat service服务，可以在电脑服务中找到这个服务

![image-20201102175325788](D:\马士兵架构\myProject\系统IO\images\image-20201102175325788.png)

代码中new线程在linux中其实就是clone的系统调用

###### 整个BIO的弊端是什么？阻塞 BLOCKING 

系统调用有个设置 socker中 non_BLOCKING  ![image-20201102175713158](D:\马士兵架构\myProject\系统IO\images\image-20201102175713158.png)

```
ServerSocketChannel ss = ServerSocketChannel.open();  //服务端开启监听：接受客户端
   ss.bind(new InetSocketAddress(9090));
   ss.configureBlocking(false); //重点  OS  NONBLOCKING!!!  //只让接受客户端  不阻塞
   false的时候，SocketChannel client = ss.accept()方法不会阻塞 -1 null
   //accept  调用内核了：1，没有客户端连接进来，返回值？在BIO 的时候一直卡着，但是在NIO ，不卡着，返回-1，NULL
   //如果来客户端的连接，accept 返回的是这个客户端的fd  5，client  object
   //NONBLOCKING 就是代码能往下走了，只不过有不同的情况
   client.configureBlocking(false); //重点  socket（服务端的listen socket<连接请求三次握手后，往我这里扔，我去通过accept 得到  连接的socket>，连接socket<连接后的数据读写使用的> ）
   
```

ulimit -n 1024 设置最多创建1024个文件描述符，控制普通用户的

![image-20201123162832584](D:\马士兵架构\myProject\系统IO\images\image-20201123162832584.png)



cat /proc/sys/fs/file-max  查看os内核级别创建最大文件描述符

vi /etc/security/limits.conf  可以配置各个用户可使用的最大文件描述符个数

# IO 第五节

nio优势：通过1个或者几个线程，来解决N个io连接的处理问题

C10k

每次循环一次：On复杂度recv 很多调用时无意义的，浪费的，调用：计组 系统调用

read 无罪 无效的 无用的read别调用

#### 多路复用 一次调用把很多条路的信息返回

![image-20201123172403796](D:\马士兵架构\myProject\系统IO\images\image-20201123172403796.png)

#### 多路复用详解 select 系统调用有文件描述符限制不能超过1024 和poll 都是一样的单调用，一次传入fds（多个文件描述符）

NIO 每次遍历过程成本在用户态内核态切换 他的代码是自己记录了socket，然后死循环去一直read

select poll 多路复用 是一次性传入所有的文件描述符交给内核返回有数据的fd，然后自己再去WR

![image-20201123173328710](D:\马士兵架构\myProject\系统IO\images\image-20201123173328710.png)

具体信息

![image-20201123173739202](D:\马士兵架构\myProject\系统IO\images\image-20201123173739202.png)



























































