--[[
这一段主要改编自https://www.jianshu.com/p/e93247935211
 
在nginx的server中增加

 location ~ ^/DYPROXYSET {

	default_type 'text/plain';
	content_by_lua_file /data/lua/set_shared_data.lua;
}

]]
local cjson=require "cjson.safe"
local dip = ngx.shared.shared_data

local uri_args= ngx.req.get_uri_args()
ngx.log(ngx.ERR,"method=",uri_args["method"])
if uri_args["method"] == "add" then
		--[[
			对于add 方法，它的请求格式为GET 方法请求
			?method=add&&domain=examples&&rip=10.20.30.40:8080
		]]
	if uri_args['domain'] == nil then
	       ngx.say("domain is null")
	else
		--[[
			在进行增加的时候，需要进行内容的拼接，而不是替换
		]]
		local hased_servers=dip:get(uri_args['domain'])
		if hased_servers ==nil then 
			dip:set(uri_args["domain"],uri_args["rip"]) 
		else
				local _,d=string.find(hased_servers,uri_args['rip'])
				if d==nil then 
					--串一起
  					dip:set(uri_args['domain'],hased_servers..','..uri_args['rip']) 
				end 
		end
		ngx.say("Set successfully : domain"..uri_args["domain"].."|rip"..uri_args["rip"])
	end
elseif uri_args["method"]=="del" then
	--[[
	del 方法的请格式为 ?method=del&&domain=user&&rip=10.20.39.45:9090
	]]
	if uri_args["domain"] == nil then
		ngx.say("domain is null")
	else
		--[[
			因为所有的服务都是以字符串的形式，存储在dip中以context_path为键，以host(多个host以逗号分隔)
			为value的形式进行存储，所以删除不能全删除，只能删掉想要删除的，
			所以这里有逻辑处理，比如，我原来的context_path=user的服务有
			4台机器，这里删除，只根据content的内容进行删除，而不是全删掉
		]]
		local hased_servers=dip:get(uri_args['domain'])
		if hased_servers ==nil then 
			ngx.say(uri_args["domain"] .. 'not hased servers')
		else
			local s,d=string.find(hased_servers,uri_args['rip'])
			if d ~=nil then
				--我们需要删除这个地址
				local new_servers=string.sub(hased_servers,1,s-1)..string.sub(hased_servers,d+1)
				if string.len(new_servers)==0 then
				 		dip:delete(uri_args["domain"])
				 else 
				 	   dip:set(uri_args['domain'],new_servers)
				 end 
			end
		end 
		ngx.say("del successfully")
	end
elseif ngx.req.get_method()=='POST' then

	--[[
	请求方法为post
		请求格式为：
		{
			"all":[{"examples":"10.20.30.40:9090",
						"docs":"10.20.30.40:9089,10.20.30.40:9099"
						}]
		}
	]]
	ngx.req.read_body();
	local post_args = ngx.req.get_post_args()  
	local json_contents=cjson.decode(post_args)
	if json_contents ==nil then
		ngx.say('post_args is not json format')
		return
	end 
	local domain_hs=json_contents['all']
		--这是个数组遍历一下
	for dm,hos in pairs(domain_hs) do 
			dip:set(dom,hos)
	end 
	ngx.say('post all hosts successfully')
end


