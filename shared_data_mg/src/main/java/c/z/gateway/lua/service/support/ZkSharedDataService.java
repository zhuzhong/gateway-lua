/**
 * 
 */
package c.z.gateway.lua.service.support;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import c.z.gateway.lua.service.HttpClientService;
import c.z.gateway.lua.service.SharedDataService;

/**
 * @author sunff
 *
 */
@Service
public class ZkSharedDataService implements SharedDataService {

	@Autowired
	private SharedDataConfig config;

	@Autowired
	private HttpClientService httpClientService;

	@Override
	public boolean synAllServersData() {
		return pushSharedData();
	}

	private boolean pushSharedData() {
		/*
		 * 请求格式为： { "all":[{"examples":"10.20.30.40:9090",
		 * "docs":"10.20.30.40:9089,10.20.30.40:9099" }] }
		 */
		JSONObject subObject = new JSONObject();
		for (Map.Entry<String, Set<String>> kv : hosts.entrySet()) {
			SharedData s = new SharedData(kv.getKey(), filterServers(kv.getKey(),kv.getValue())
					                     );
			subObject.put(s.getContextPath(), s.getHostsString());
		}

		JSONObject jobject = new JSONObject();
		jobject.put("all", subObject);
		//logger.info("下面使用httpclient将其push 到nginx即可,后面再写...");
		String s = jobject.toJSONString();
		logger.info("push到nginx的内容={}", s);
		String ret = httpClientService.doPost(config.getPushSharedDataAddress(), s);
		logger.info("push host servers to gateway return value={}", ret);
		return true;

	}

	protected Set<String> filterServers(String contextpath,Set<String> servers) {
		/**
		 * 对于限流的服务器，不需要再次上线的，则在此过滤掉
		 */
		return servers;
	}
	private static class SharedData {
		private final String contextPath;
		private final List<String> hosts;

		/**
		 * @param contextPath
		 * @param hosts
		 */
		public SharedData(String contextPath, Set<String> hosts) {
			super();
			this.contextPath = contextPath;
			this.hosts = new ArrayList<String>(hosts);
		}

		public String getContextPath() {
			return contextPath;
		}

		public String getHostsString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < hosts.size(); i++) {
				if (i == hosts.size() - 1) {
					sb.append(hosts.get(i));
				} else {
					sb.append(hosts.get(i));
					sb.append(",");
				}
			}
			return sb.toString();
		}
	}

	// 准备数据部分
	private static Logger logger = LoggerFactory.getLogger(ZkSharedDataService.class);

	private static final String REST = "rest";

	private static final String PROVIDERS = "providers";

	private static final String REST_SLASH = REST + "://";
	private static final String SLASH = "/";
	private static final String UTF_8 = "utf-8";

	private ZkClient zkClient;

	@PostConstruct
	public void init() {
		zkClient = new ZkClient(config.getZkServers(), 5000);
		String rootPath = config.getRootPath();
		if (!rootPath.startsWith(SLASH)) {
			rootPath = SLASH + rootPath;
		}
		runaway(zkClient, rootPath);
	}

	private static ConcurrentHashMap<String/* contextPath */, Set<String/* host:port */>> hosts = new ConcurrentHashMap<String, Set<String>>();

	private void runaway(final ZkClient zkClient, final String path) {
		zkClient.unsubscribeAll();
		ConcurrentHashMap<String, Set<String>> newHosts = new ConcurrentHashMap<String, Set<String>>();
		zkClient.subscribeChildChanges(path, new IZkChildListener() {

			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				/*
				 * System.out.println(parentPath + " 's child changed, currentChilds:" +
				 * currentChilds);
				 */
				logger.info("{}'s child changed, currentChilds:{}", parentPath, currentChilds);
				// 一级节点的子节点发生变化
				runaway(zkClient, path); // 重新再来

			}

		});

		List<String> firstGeneration = zkClient.getChildren(path); // 二级节点
																	// /dubbo-online/com.z.test.Testapi
		if (firstGeneration != null && firstGeneration.size() > 0) {
			for (String child : firstGeneration) {
				String firstNextPath = path + "/" + child;
				zkClient.subscribeChildChanges(firstNextPath, new IZkChildListener() {

					public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
						/*
						 * System.out.println(parentPath + " 's child changed, currentChilds:" +
						 * currentChilds);
						 */
						logger.info("{}'s child changed, currentChilds:{}", parentPath, currentChilds);
						// 2级节点的子节点发生
						runaway(zkClient, path); // 重新再来

					}

				});

				List<String> secondGeneration = zkClient.getChildren(firstNextPath); // 三级子节点
																						// /dubbo-online/com.z.test.Testapi/providers
				if (secondGeneration != null && secondGeneration.size() > 0) {
					for (String secondChild : secondGeneration) {
						if (secondChild.startsWith(PROVIDERS)) {
							String secondNextPath = firstNextPath + "/" + secondChild;
							zkClient.subscribeChildChanges(secondNextPath, new IZkChildListener() {

								public void handleChildChange(String parentPath, List<String> currentChilds)
										throws Exception {
									/*
									 * System.out .println(parentPath + " 's child changed, currentChilds:" +
									 * currentChilds);
									 */
									logger.info("{}'s child changed, currentChilds:{}", parentPath, currentChilds);
									// 3级节点的子节点发生
									runaway(zkClient, path); // 重新再来

								}

							});

							List<String> thirdGeneration = zkClient.getChildren(secondNextPath);// 4级子节点
																								// /dubbo-online/com.z.test.Testapi/rest://localhost:8080
							if (thirdGeneration != null && thirdGeneration.size() > 0) {
								for (String thirdChild : thirdGeneration) {
									if (thirdChild.startsWith(REST)) {
										/*
										 * 样例 rest://10.148.16.27:8480/demo/ com.z.m.facade.api. DemoFacadeService
										 */
										ServiceProvider sp = new ServiceProvider(thirdChild);
										String contextPath = sp.getContextPath();
										String host = sp.getHost();
										Set<String> hostSets = newHosts.get(contextPath);
										if (hostSets == null) {
											hostSets = new HashSet<String>();
											newHosts.put(contextPath, hostSets);
										}
										hostSets.add(host);
									}
								}
							}
						}
					}
				}
			}

		}

		synchronized (this) {
			hosts.clear();
			hosts.putAll(newHosts);
			if (!hosts.isEmpty())
				pushSharedData();
		}
	}

	private static class ServiceProvider {

		private String host;
		private String contextPath;
		private String provider;

		public ServiceProvider(String provider) {
			try {
				this.provider = URLDecoder.decode(provider, UTF_8);
			} catch (UnsupportedEncodingException e) {
				logger.error("地址解码错误{}", e);
				this.provider = provider;
			}
			parse();
		}

		private void parse() {
			String subString = provider.substring(REST_SLASH.length());

			int indexOfFirstSlash = subString.indexOf(SLASH);

			host = subString.substring(0, indexOfFirstSlash);
			String subSubString = subString.substring(indexOfFirstSlash + 1);
			int indexOfSecondSlash = subSubString.indexOf(SLASH);
			contextPath = subSubString.substring(0, indexOfSecondSlash);
		}

		public String getHost() {
			return host;
		}

		public String getContextPath() {
			return contextPath;
		}

	}

}
