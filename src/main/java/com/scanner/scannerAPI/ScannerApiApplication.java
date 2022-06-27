package com.scanner.scannerAPI;

import java.sql.SQLException;

import com.scanner.scannerAPI.DatabaseConnector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@SpringBootApplication 
@RestController
public class ScannerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScannerApiApplication.class, args);
	}
	// endpoint

	// @RequestMapping("/")
    // public ModelAndView welcome() {
    //     ModelAndView modelAndView = new ModelAndView();
    //     modelAndView.setViewName("index.html");
    //     return modelAndView;
    // }  

	// @RequestMapping("/css/styles.css")
    // public ModelAndView stylesheetMain() {
    //     ModelAndView modelAndView = new ModelAndView();
    //     modelAndView.setViewName("styles.css");
    //     return modelAndView;
    // }  


	@GetMapping("/agency/{agencyName}")
	public String get(@PathVariable String agencyName) {
		try {
			return DatabaseConnector.getWeeklyData(agencyName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "[]";
	}

	@GetMapping("/hourly/{callType}")
	public String hello(@PathVariable String callType) {
		try {
			return DatabaseConnector.getHourlyCallData(callType);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "[]";
	}

}
