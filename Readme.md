# gateway-lua


基本openresty，采用lua开发dubbo restful服务的网关系统。思路采用nginx的proxy_pass可用使用变量的特性来动态的进行后端服务路由。



## 使用方式



###  安装openresty


### 修改nginx.conf
	
将nginx.conf进行如下配置，本文档只展示主要的配置。