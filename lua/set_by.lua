
local math=require "math"

--字符串分隔方法
function string:split(sep)
		local sep, fields = sep or ":", {}
		local pattern = string.format("([^%s]+)", sep)
		self:gsub(pattern, function (c) fields[#fields + 1] = c end)
		return fields
end


local function s2table(s)
	local ts = {}
	ngx.log(ngx.ERR,"s=",s)
	local server_arrays=s:split(',')
	for i=1,#server_arrays do

		local s=server_arrays[i]
		if s~=nil then 
			ts[i]=s
		end 
	end	

	table.sort(ts,function ( a,b )
		-- body
					if a<b then 	
						return true;
					elseif a>=b then 
							return false;
					end
			end)
	return ts
end

-- 随机算法
local function rdom_load_balance_server( ts )
	-- body
	local t_length=table.getn(ts);
	ngx.log(ngx.ERR,'t_length=',t_length);
	--设置时间种子
	math.randomseed(tostring(os.time()):reverse():sub(1, 7)) 
	local r=math.random(1,t_length)
	ngx.log(ngx.ERR,'r=',r);
	return ts[r]
end


function get_server( context_path )
	local shared_data=ngx.shared.shared_data
	local servers=shared_data:get(context_path)
	if servers ==nil then
		ngx.log(ngx.ERR,'getnullservers=',servers)
		--return "http://127.0.0.1:10000"
		return get_server('local')
	end
	ngx.log(ngx.ERR,"server=",servers)
	local ts=s2table(servers)
--然后实现随机负载均衡算法,选择其中一个服务端
	local r_server=rdom_load_balance_server(ts)
	if r_server ==nil then 
		--[[
		对于动态服务找不相应的后端服务器，全部指定到一台静态服务器上,这个根据业务具体决定,
		如何处理
		]]
		r_server="127.0.0.1:10000"
	end 
		return 'http://'..r_server
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
if e == nil then
	return get_server('local')
end
--ngx.log(ngx.ERR,"i=",i," e=" ,e)
local sub_request_uri=string.sub(request_uri,i+1)

if string.len(sub_request_uri) == 0 then
	return get_server('local')
end

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

return get_server(context_path)


