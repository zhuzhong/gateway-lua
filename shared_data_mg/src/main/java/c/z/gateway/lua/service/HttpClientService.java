package c.z.gateway.lua.service;

import java.util.Map;

/**
 * @author Administrator
 *
 */
public interface HttpClientService {

	public String doGet(String webUrl);

	public String doPost(String url, String reqData);

	public String doGet(String webUrl, Map<String, String> paramMap);

}