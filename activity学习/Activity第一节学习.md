# Activity第一节学习

什么事bpm，可以理解成一种管理模式

bpmn主要有两个版本，2004年发布的1.0，2011年发布的2.0,用来实现这个bpm的一种规范

工作流指的是 业务过程的部分或整体在计算机应用环境下的自动化，是对工作流程及各步骤之间的业务规则的抽象，概括描述

员工请假--------部门主管审批------经理审批-------老板

工作流引擎  简单理解工作流是概念，工作流引擎就是工作流的实现

应用方面，1.办公软件  2.CRM系统  3.ERP系统  4.OA系统

https://www.openhub.net/ 这个网站可以进行对比

工作流发展 jbpm------activiti-----flowable     camunda 基于activiti5比较大的贡献值

主讲 ACtiviti  很多公司主要5.52为主流

流程事件

![image-20200727132603383](D:\马士兵架构\myProject\activity学习\images\image-20200727132603383.png)

instances 流程实例  deployments部署 definitions流程定义  task 任务 job 定时任务

![image-20200727140742427](D:\马士兵架构\myProject\activity学习\images\image-20200727140742427.png)



1.运行环境对流程有影响

2.使用springboot配置



activiti 6 默认28张   7默认只有17张表（去掉了两个service），默认不再新建用户表那些 FormService 这个再activiti 7中没用了

RepositoryService RuntimeService TankService HistoryService 这四个是经常用的 

ManagermentService 管理服务，内部使用的



activiti 7默认集成了springsecrity的东西，

 activiti 6 配置一个数据源就可以启动，因为他都有默认值的

































