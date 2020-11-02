# 系统IO学习笔记 第二节

推荐书籍：深入理解linux内科，深入理解计算机系统

epoll《 怎么知道数据到达    中断   

虚拟地址（线性地址）中app1的地址是线性排列的，他们对应的物理内存中的物理地址是分配在不同地址上的，之前有一个映射关系，通过cpu中mmu内存管理单元通过程序提供给它的映射关系换算表来计算地址转换，依赖于page 4kb ，随用随分配，不会全量分配，所以会有缺页，cpu会有一个缺页异常去把这个页补上才会继续执行

一个进程加载的顺序都是一个page一个page挨个加载的

不同程序访问读取相同的文件，其实都是公用的内存中pagecache，程序中维护了一个seek表，已经读取数据的情况

page cache 内核维护 中间层  使用多大内存    是否淘汰    修改之后时候延时，是否丢数据？-----> page cache优化IO性能  丢失数据

![image-20201028100133653](D:\马士兵架构\myProject\系统IO\images\image-20201028100133653.png)

##### 脏页测试

sysctl -a  会显示linux中的配置项

```
vm.dirty_background_bytes = 0
vm.dirty_background_ratio = 10    百分比 占用10的内存的时候linux内核就会把数据写入到磁盘，它是不会阻塞的   后台 后台百分比一般比前台小
vm.dirty_bytes = 0
vm.dirty_expire_centisecs = 3000  脏数据可以保存30s
vm.dirty_ratio = 30     程序分配实际内存达到百分之30的时候就会阻塞程序  前台
vm.dirty_writeback_centisecs = 500 5s进行一次项磁盘写数据
```

sysctl -p  修改sysctl之后要执行，启动一下

vi /ect/sysctl.conf  里面添加dirty设置

```
rm -rf *out.*
/opt/jdk1.8.0_221/bin/javac OSFileIO.java
strace -ff -o out /opt/jdk1.8.0_221/bin/java OSFileIO $1-----这个是传递的参数
```

ll -h && ./pcstat out.txt

buffIO快还是普通IO快，buffer快，因为buffer是存到一定的数据量之后再刷新

ll -h && ./pcstat out.txt && ./pcstat ooxx.txt  这个可以观察数据变化，也就是ooxx中percent中数值边慢慢的变小，也就是说ooxx缓存页已经被out.txx中使用llr淘汰机制进行淘汰了，这些淘汰的page是别人使用的，肯定不是

脏数据

###### 淘汰都是先刷新到本地磁盘，再淘汰内存的page，淘汰的page肯定不是脏的，是已经写到磁盘上去的，是干净的

![image-20201028112337104](D:\马士兵架构\myProject\系统IO\images\image-20201028112337104.png)

cpu在没有DMA的时候，是把数据的一部分放在cpu的寄存器，从cpu的数据总线交给硬盘，会转圈从cpu转出去，这个时候cpu是不能对其他提供服务的，他这个时候一直在忙IO的事情

有DMA的时候，就是把地址总线给它，由它来控制地址总线一直传数据，选址---总线----搬运数据的过程都有DMA来完成，cpu就可以去处理其他程序了

```
buffer.put("123".getBytes());  插入123到buffer中去这个时候 pos=3 limit=1024 cap=1024
buffer.flip();   //读写交替    pos=0 limit=3 cap=1024 也就是读写都要用到pos指针
buffer.get();	获取一个字节  pos=1  limit=3 cap=1024
buffer.compact();  把读取了的空间挤压出去，重新排序，并且进入写模式 pos=2 limit=1024 cap=1024
buffer.clear();  pos=0 limit=1024 cap=1024
```

FileChanel 只有文件chanel才有map方法，mmap 堆外 和文件映射的 只有byte 没有对象的概率

```
FileChannel rafchannel = raf.getChannel();
        //mmap  堆外  和文件映射的   byte  not  objtect
        MappedByteBuffer map = rafchannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);


        map.put("@@@".getBytes());  //不是系统调用  但是数据会到达 内核的pagecache
            //曾经我们是需要out.write()  这样的系统调用，才能让程序的data 进入内核的pagecache
            //曾经必须有用户态内核态切换
            //mmap的内存映射，依然是内核的pagecache体系所约束的！！！
            //换言之，丢数据
            //你可以去github上找一些 其他C程序员写的jni扩展库，使用linux内核的Direct IO
            //直接IO是忽略linux的pagecache
            //是把pagecache  交给了程序自己开辟，一个字节数组当作pagecache，动用代码逻辑来维护一致性/dirty。。。一系列复杂问题
            map.force(); //  flush
```



##### 这就是为什么 java你要先从堆内的数据拷贝到java进程的堆内（也就是jvm里面的堆里拷贝到c进程的堆里面），然后再拷贝到内核的空间里，再拷贝到相应的设备里面去

lsof -p 10197 查看pid进程情况  这个就是执行了上面的map方法之后调用了mmap系统调用

![image-20201028141424342](D:\马士兵架构\myProject\系统IO\images\image-20201028141424342.png)

代码段 ，数据段 堆空间 mmap系统调用分配的映射空间   stack栈空间

on heap（JVM堆里）中要写数据到磁盘，肯定是要走系统调用的 channel read 或者write ，但是jvm堆内的数据肯定是要先到堆外去，然后再通过系统调用拷贝到pagecache

off heap堆外内存数据（java进程的堆里） 就可以直接通过系统调用channel read /write 拷贝到pagecache中

![image-20201028142301299](D:\马士兵架构\myProject\系统IO\images\image-20201028142301299.png)

kafka ES 都需要副本 这个时候也就是socket 它也是IO  ，副本也会涉及到同步/异步的区分





































