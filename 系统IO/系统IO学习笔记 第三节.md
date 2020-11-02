# 系统IO学习笔记 第三节

网络IO TCP 参数

lsof -p 查看进程文件描述符分配情况   netstat -natp    tcpdump 抓取网络数据包

###### tcpdump -nn -i enh33(网卡的名称) port 9090 抓取网络数据包

![image-20201028151030294](D:\马士兵架构\myProject\系统IO\images\image-20201028151030294.png)

TCP是针对建立连接的，客户端Socket建立连接，就算是服务端没有任何进程接受这个socket它也是能够成功建立连接，三次握手的，并且发送的数据，linux内核也是能够接受的并且回复ACK确认，通过这个Recv-Q显示接受了多少个字符

TCP------面向连接的，可靠的传输协议--------三次握手----------------内核级开辟资源

也就是说三次握手之后双方就会开始开辟资源，并且为双方提供服务

###### Socket---四元组（客户端ip—客户端端口—服务端ip—服务端的端口）---内核级-----即便你不调用accept

服务端是否需要为client的连接分配的一个随机端口号？不需要

###### 只要四元组中有一个不同都可以建立连接

![image-20201028153132521](D:\马士兵架构\myProject\系统IO\images\image-20201028153132521.png)

netstat -natp中   ESTABLISHED代表已经完成三次握手  SYN_RECV 代表客户端发来握手请求，但是我没有理他  LISTEN 代表正在监听中

```
server = new ServerSocket();
server.bind(new InetSocketAddress(9090), BACK_LOG);
server.setReceiveBufferSize(RECEIVE_BUFFER);
server.setReuseAddress(REUSE_ADDR);
server.setSoTimeout(SO_TIMEOUT);  //设置超时时间 服务端accept会在等待超时时间之后抛出异常
```

BACK_LOG就是代表你可以建立BACK_LOG+1个连接，就算是没有分配给具体的线程，但是最好没有设置过大，因为太长时间没有分配处理的线程，这个连接还是会出问题

```
  // System.in.read();  //分水岭：

Socket client = server.accept();  //阻塞的，没有 -1  一直卡着不动  accept(4,
System.out.println("client port: " + client.getPort());
client.setKeepAlive(CLI_KEEPALIVE);
client.setOOBInline(CLI_OOB);
client.setReceiveBufferSize(CLI_REC_BUF);
client.setReuseAddress(CLI_REUSE_ADDR);
client.setSendBufferSize(CLI_SEND_BUF);
client.setSoLinger(CLI_LINGER, CLI_LINGER_N);
client.setSoTimeout(CLI_TIMEOUT);  读取信息时超时时间，防止恶意连接，只连接不发送请求头
client.setTcpNoDelay(CLI_NO_DELAY);
```

数据包多大？ MTU 通过ifconfig中查看一般是1500  tcpdump中MSS就是你数据包实际大小

三次握手之后会协商出一个win窗口大小，客户端服务端发送数据的时候都会带着一个win窗口大小告诉对方我还有多少个空格可以装数据  ，这就是窗口机制，他解决了TCP拥塞----也就是说如果你服务端的窗口被填满了，那么你会的数据包里面会告诉客户端我没有余量了，这个时候客户端就是暂停给你发包，当服务端内核把数据包处理了，有空间的时候会再发一次数据包过去告诉客户端，这边可以继续接受数据包了

所以说如果通信的时候控制不好这些，是会丢失数据的，可以测试看：服务端程序启动，但是不accept，这个时候使用nc去连接然后疯狂发送数据，他会到达一个数据量大小之后不会再继续增长了就开始丢失数据了

```
Socket client = new Socket("192.168.150.11",9090);
client.setSendBufferSize(20); buffer长度
client.setTcpNoDelay(true);  代表不开启就是每次都只发出写入的数据 false代表开启优化，一次发出多个字符，会攒数据一次发出
client.setOOBInline(false);  是不是立即发出第一个数据字节
```

keepalive-----TCP如果双方建立了连接？很久都不说话？对方还活着吗？这个时候就提出了一个keepalive心态的机制

## 网络IO 变化 模型

```
同步
异步
阻塞
非阻塞
strace -ff -o out cmd命令（java SocketIO） 追踪系统调用的信息
```

###### 

然后子线程中recv

man 7 ip   man tcp  man bash   man man 查看具体信息

##### BIO 连接详解图

在系统调用中 new thread 其实就是操作系统中的一个子进程通过clone命令并且共享了他主进程的资源，clone指令会指定共享文件描述符，打开的文件，VM等等，其实clone就是fork经过包装的命令

![image-20201028165236794](D:\马士兵架构\myProject\系统IO\images\image-20201028165236794.png)





























