package c.z.gateway.lua;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

	@RequestMapping(value = "/home")
	@ResponseBody
	public String home() {
		System.out.println("redirect to home page!");
		return "indexok";
	}

	@RequestMapping(value = "/home/page")
	@ResponseBody
	public ModelAndView goHome() {
		System.out.println("go to the home page!");
		ModelAndView mode = new ModelAndView();
		mode.addObject("name", "zhangsan");
		mode.setViewName("index");
		return mode;
	}
}
