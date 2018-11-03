package c.z.gateway.lua;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import c.z.gateway.lua.service.SharedDataService;

@Controller
@RequestMapping("sharedData")
public class SharedDataController {

	@Autowired
	private SharedDataService sharedDataService;

	@RequestMapping(value = "/all")
	@ResponseBody
	public String home() {
		boolean r = sharedDataService.synAllServersData();
		if (r) {
			return "push all ok";
		} else {
			return "push not ok";
		}
	}

	@RequestMapping(value = "/add")
	@ResponseBody
	public String goHome() {

		return "delsuccess";
	}
}
