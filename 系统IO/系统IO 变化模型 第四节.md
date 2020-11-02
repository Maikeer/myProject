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













































