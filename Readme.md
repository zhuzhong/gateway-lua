# gateway-lua


基本openresty，采用lua开发dubbo restful服务的网关系统。思路采用nginx的proxy_pass可用使用变量的特性来动态的进行后端服务路由。

## 使用方式

###  安装openresty
1. 增加/etc/yum.repos.d/OpenResty.repo文件的内容为
	
	[openresty]
	name=Official OpenResty Repository
	
	baseurl=https://openresty.org/yum/openresty/openresty/epel-$releasever-$basearch/
	
	skip_if_unavailable=True
	
	gpgcheck=1
	
	gpgkey=https://copr-be.cloud.fedoraproject.org/results/openresty/openresty/pubkey.gpg
	
	enabled=1
	
	enabled_metadata=1
	
1. sudo yum install openresty

### 修改nginx.conf
	
nginx.conf进行如下配置(只展示主要的配置)
	
1. 在nginx.conf 的http 中增加如下内容
		 
	
	    #共享空间大小根据实际情况进行增加
		 lua_shared_dict shared_data 10m;
  		 init_by_lua_file /data/lua/init_by.lua;

	   	 server {
	        listen       80;
	        server_name  192.168.31.120 localhost;
			charset utf-8;
	       # access_log  logs/host.access.log  main;
	
	        location / {  	
				default_type "text/html";
				set_by_lua_file $back_server /data/lua/set_by.lua;
				proxy_set_header Host $host;
				proxy_http_version 1.1;
				proxy_pass $back_server;
	        }
	
		
		 location ~ ^/DYPROXYSET {
				default_type 'text/plain';
				content_by_lua_file /data/lua/set_shared_data.lua;
			}
		
		}
	
	
		  server {
	        listen       2018;
		     server_name localhost 127.0.0.1;
	        location / {
	            root   html;
	            index  index.html index.htm;
	        }
	    }
    
    
    
## 关于shared_data的初始化的想法

现在是通过init_by.lua 进行的初始化，并且只是静态的。那么如何动态的初始化它或者更新它呢？具体的方案需要根据dubbo的注册中心所采用的方案而作相应的变化。本系统主要以zookeeper作为dubbo的注册中心进行方案的设计，原因是zookeeper作为dubbo的注册中心的使用比例之高，以及它的负载均衡、容错错特点，对于系统的高可用保证。

假定dubbo以zookeeper作为服务的注册中心，则获取zookeeper中的方式，目前了解下来有两个方案，一是直接使用zklua在openresty这一端直接获取相应的zookeeper节点中的数据；二是采用中间服务的形式获取zookeeper节点的数据问题，然后再写入shared_data中即可。第二种方案相比第一种方案来说有些啰嗦，但是个人认为第二种方案的可扩展性比较强，对于后面的服务降级限流等都有很大的可操作空间。所以后续以第二种方案进行开发。

##手动管理shared_data的方法
1. 新增后端服务器

		GET请求
		http://192.168.31.120/DYPROXYSET?method=add&&domain=examples&&rip=127.0.0.1:8089
		
2. 删除后端服务器
	
		GET请求
		http://192.168.31.120/DYPROXYSET?method=del&&domain=examples&&rip=127.0.0.1:8089

1. 重新加载全部服务器或者初始化服务器

		POST请求
		http://192.168.31.120/DYPROXYSET
	
		{
			"all":[{"examples":"10.20.30.40:9090",
						"docs":"10.20.30.40:9089,10.20.30.40:9099"
						}]
		}

## 自动管理sharded_data的方法
	参见
  