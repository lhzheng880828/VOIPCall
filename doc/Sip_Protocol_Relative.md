# 1. SIP相关知识点 

SIP协议中的认证方式，处理401或者407

协议及原理
https://www.cnblogs.com/fengyv/archive/2013/02/05/2892729.html
具体代码实现
https://bbs.csdn.net/topics/360193260

mjsip下的401和407认证问题
https://blog.csdn.net/b02330224/article/details/6759012
SIP 的注册流程，认证算法
https://blog.csdn.net/kai157346113/article/details/78537159

多媒体通话杂谈：SIP安全通话对于401和407的处理
http://blog.sina.com.cn/s/blog_4ca9ceef0102xmvt.html

SIP注册信令消息示范及解释
https://blog.csdn.net/xiaoborui20110806/article/details/40341497

SIP常用消息实例参考 1、REGISTER消息
https://blog.csdn.net/xiaoborui20110806/article/details/40422931

SIP常用消息实例参考 2、INVITE消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423009

SIP常用消息实例参考 3、MESSAGE消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423039


SIP常用消息实例参考 4、SUBSCRIBE消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423065

SIP常用消息实例参考 5、ACK消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423095

SIP常用消息实例参考 6、BYE消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423127

SIP常用消息实例参考 7、PRACK消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423155

SIP常用消息实例参考 8、INFO消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423163

SIP常用消息实例参考 9、OPTIONS消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423183

SIP常用消息实例参考 10、NOTIFY消息
https://blog.csdn.net/xiaoborui20110806/article/details/40423201

SIP知识专辑
https://blog.csdn.net/b02330224/category_883565.html

# 2. 抓包
3370上抓包命令 
$ tcpdump -i eth0 -w sipcall.pcap -W 1 -C 10000000 -s 0
C03抓包命令
$ tcpdump -s0 -w /tmp/sipdata.pcap -i eth0
包路径SipCall.pcap

