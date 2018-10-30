local math=require "math"
local function s2table(servers)
	table ts={}
	--如果servers 不是多个，而是一个 split的行为是什么呢？测试一下
	local server_arrays=string.split(servers,',')
	for i=1;#server_arrays do
		local s=server_arrays[i]
		ts[i]=s
	end	

	table.sort(ts,function ( a,b )
		-- body
					if a<b then 	
						return true;
					else a>=b then 
							return false;
					end
			end)
	return ts
end

local function rdom_load_balance_server( ts )
	-- body
	local t_lenth=table.getn(ts);
	math.randomseed(t_lenth)
	local r=math.random(t_lenth)
	return ts[r]
end

--------------------------------------------------------------------------------
--[[
根据请求的url地址 比如
http://localhost/context_path/user/queryUserInfo/1
从中截取context_path部分，然后路由至相应的后端服务器

]]
-- 请求的url地址
local request_uri=ngx.var.request_uri
ngx.log(ngx.ERR,"request_uri=",request_uri)
--local uri=ngx.var.uri
--ngx.log(ngx.ERR,"uri=",uri)
local i,e=string.find(request_uri,"/")
--ngx.log(ngx.ERR,"i=",i," e=" ,e)
local sub_request_uri=string.sub(request_uri,i+1)
ngx.log(ngx.ERR,"sub_request_uri=",sub_request_uri)

local _,d=string.find(sub_request_uri,"/")
local context_path
if d ~=nil then
           context_path=string.sub(sub_request_uri,1,d-1)
           ngx.log(ngx.ERR,"c=",context_path," d=",d)
else
	context_path=sub_request_uri;
	ngx.log(ngx.ERR,"c2=",context_path)
end


local shared_data=ngx.shared.shared_data
local servers=shared_data:get(context_path)
ngx.log(ngx.ERR,"server=",servers)


local ts=s2table(servers)
--然后实现随机负载均衡算法,选择其中一个服务端
local r_server=rdom_load_balance_server(ts)
return 


if r_server ==nil then 
	r_server="127.0.0.1:8088"
end 

return "http://"..r_server

