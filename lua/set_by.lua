--[[
根据请求的url地址 比如
http://localhost/context_path/user/queryUserInfo/1
从中截取context_path部分，然后路由至相应的后端服务器

]]
--local gw="gw"
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
local server=shared_data:get(context_path)
ngx.log(ngx.ERR,"server=",server)
if server ==nil then 
	server="127.0.0.1:8088"
end 

return "http://"..server

