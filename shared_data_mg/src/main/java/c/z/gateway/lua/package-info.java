
/**
 * @author sunff
 *
 *本系统的功能是动态的维护openresty中lua_shared_dict shared_data内存区域的内容。
 *即从dubbo服务的注册中心zookeeper获取对应的rest服务信息，然后同步到shared_data中，
 *
 */
package c.z.gateway.lua;