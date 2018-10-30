
local shared_data=ngx.shared.shared_data

shared_data:set("examples","127.0.0.1:8088")
shared_data:set("docs","127.0.0.1:8089")
shared_data:set("local","127.0.0.1:10000")

