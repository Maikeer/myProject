# 系统IO学习笔记

首先df命令可以查看linux系统关于目录来自哪个文件磁盘系统的 

umount  取消挂载  mount  挂载命令  

冯洛伊曼 计算器，控制器，主寄存器，输出输入设备 IO----抽象 一切皆文件-------------I/O

目录树结构趋向于稳定有一个映射的过程------文件类型-----------1.-：普通文件（可执行，图片，文件）REG

d：目录 l：连接 b：块设备 c：字符设备 CHR s：socket p：pipeline [eventpoll]:内核提供的内存区域

ln 路径1 路径2 硬连接  修改任意一个数据其他都会看到  删除源文件路径1的时候，路径2不会消失不会报错

ln -s 软连接  修改任意一个数据其他都会看到  删除源文件路径1的时候，路径2不会消失但是会出现报错，源文件丢失

stat 可以看到文件的元数据信息

dd if=/dev/zero of=mydisk.img bs=1048576 count=100 创建一个100M的文件

if=具体的磁盘路径/dev/sda1 of=/dev/sda3  两个硬盘对拷贝

if=具体的磁盘路径/dev/sda1 of=mydisk.img文件  把整个sda1分区做了一个压缩文件备份

if=具体的磁盘路径/dev/xxx.img of=/dev/sda1  对sda1分区进行恢复

if=具体的磁盘路径/dev/zero of=mydisk.img    创建一个磁盘镜像

ll -h 查看文件已M显示大小

losetup /dev/loop0 

ldd bash 可以分析bash加载了那些文件

cp 路径1{文件1，文件2，文件3}  路径2

lsof 可以显示进程打开了那些文件

lsof -o(显示偏移量)p $$  0 1  2 标准输入 输出 报错信息 $$当前进程的id号

##### 下面的操作就是为了验证linux下同一个文件可以被内核中多个app读取，内存中的pagecache是共享的

exec 8 < ~/ooxx.txt

read  a 0<& 8 8是文件描述符

echo $a  输出变量a的数据

swap磁盘交换空间，就是内存不够的时候，可以把暂时不用的数据放到swap空间去，下次要用的时候再换出来

pcstat 

##### socket演示

exec 8<> /dev/tcp/www.baidu.com/80

lsof -op $$

![image-20201027162411564](D:\马士兵架构\myProject\系统IO\images\image-20201027162411564.png)

##### 管道 pipline  任何程序都有0 1 2 输入 输出  报错输出

$$ 当前bash的pid $BASHPID  也可以获取当前pid

 重定向：不是命令，是机制

< 输入

">"输出

管道 |

head -8 oo.txt  从头开始显示8行

tail -1 oo.txt   从尾开始显示一行

head -8 oo.txt | tail -1 显示oo.txt文件中第八行

pstree 查看进程

{echo "edasdsa";echo "sdsadsads";} 代码块会在同一线程执行

{ a=9;echo "sdsads"} | cat  bash解释的时候看到 | 就会分别在 |两边分别创建子进程进行执行并把子进程的输出通过管道连接起来

 { echo $BASHPID; read x; } | { cat ; read y; }

ps -ef | grep 9836  查看主进程生成了两个子进程

![image-20201027165343262](D:\马士兵架构\myProject\系统IO\images\image-20201027165343262.png)

cd /proc/58878/fd  可以看到输出 1 指向了一个pipe

58879 输入 0 指向了pipe

lsof -op 58878 也可以查看到数据

![image-20201027165732773](D:\马士兵架构\myProject\系统IO\images\image-20201027165732773.png)

##### PageCache  kernel折中方案

![image-20201027170034086](D:\马士兵架构\myProject\系统IO\images\image-20201027170034086.png)

cpu中有寄存器，加入你想读取一个字节的数据，如果没有优化的话就只是从硬盘拷贝到cpu，再从cpu拷贝到内核，再由内核拷贝到应用程序，这样多次拷贝速度很慢，所以加入协处理器DMA，数据不在通过cpu拷贝而是直接

由DMA计数（也就是控制地址总线大家使用一会儿）控制直接拷贝到pagecache中去

##### app1 进程是有状态的   running状态，当他调用了系统调用我要从硬盘读取东西了，这个时候cpu已经知道了，这个时候app状态已经变成保护现场了， 这个时候cpu 已经从用户态切换成内核态了，他会先从pagecache中去找，当pagecache没有的时候，会发生缺页中断，这个时候可以走DMA协处理器去完成数据的获取，这个时候我们的app1的进程就会进入一个挂起/阻塞的状态（这个时候进程调度就不会再对他进行调度，因为他不是活跃的），MDA拷贝数据完成之后，就会有个中断给cpu（因为在app1挂起的时候对这个中断进行了关注），这个时候才会把app1的状态改成可运行，在未来的某个时间段才会被调用，这个时候就会恢复现场，页已经有了，数据有了就可以继续执行了

![image-20201027171818617](D:\马士兵架构\myProject\系统IO\images\image-20201027171818617.png)



























