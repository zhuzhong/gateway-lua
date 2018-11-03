/**
 * 
 */
package c.z.gateway.lua.service.support;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author sunff
 *
 */

@Component
@ConfigurationProperties(prefix = "shareddata")
public class SharedDataConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4903926326030865340L;
	private String rootPath;
	private String zkServers;
	private String pushSharedDataAddress;

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getZkServers() {
		return zkServers;
	}

	public void setZkServers(String zkServers) {
		this.zkServers = zkServers;
	}

	public String getPushSharedDataAddress() {
		return pushSharedDataAddress;
	}

	public void setPushSharedDataAddress(String pushSharedDataAddress) {
		this.pushSharedDataAddress = pushSharedDataAddress;
	}

}
