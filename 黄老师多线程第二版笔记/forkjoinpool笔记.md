在x86里面只有store load重排序，因为有store buffer store buffer很小的

TSO针对的是单线程全局有序

volatile保证可见性吗？以后去聊volatile记住，可见性并不是它的主要作用？为什么？因为store buffer 非常小，最终由于cpu不断执行指令，会将最终的值刷入缓存，这时MESI生效，别的cpu能看到。

### java 中的volatile 最大作用就是禁止JIT（编译器）优化且指令屏障