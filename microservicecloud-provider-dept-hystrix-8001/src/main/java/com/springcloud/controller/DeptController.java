package com.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.springcloud.entities.Dept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;
import com.springcloud.service.DeptService;

import java.util.List;

@RestController
public class DeptController
{
	@Autowired
	private DeptService service;
	@Autowired
	private DiscoveryClient client;

	@RequestMapping(value = "/dept/add", method = RequestMethod.POST)
	public boolean add(@RequestBody Dept dept)
	{
		return service.add(dept);
	}

	@RequestMapping(value = "/dept/get/{id}", method = RequestMethod.GET)
	@HystrixCommand(fallbackMethod = "processHystrixGet")
	public Dept get(@PathVariable("id") Long id)
	{
		try {


		Dept dept =  service.get(id);
		if(null == dept)
		{
			throw new RuntimeException("该ID："+id+"没有没有对应的信息");
		}
		}catch (Exception e){
			String message = e.getMessage();
			System.out.println("出错了");
		}
		return service.get(id);
	}

	public Dept processHystrixGet(@PathVariable("id") Long id)
	{
		return new Dept().setDeptno(id)
				.setDname("该ID："+id+"没有没有对应的信息,null--@HystrixCommand")
				.setDb_source("no this database in MySQL");
	}


	@RequestMapping(value = "/dept/list", method = RequestMethod.GET)
	public List<Dept> list()
	{
		return service.list();
	}

	@RequestMapping(value = "/dept/discovery", method = RequestMethod.GET)
	public Object discovery()
	{
		List<String> list = client.getServices();
		System.out.println("**********" + list);

		List<ServiceInstance> srvList = client.getInstances("MICROSERVICECLOUD-DEPT");
		for (ServiceInstance element : srvList) {
			System.out.println(element.getServiceId() + "\t" + element.getHost() + "\t" + element.getPort() + "\t"
					+ element.getUri());
		}
		return this.client;
	}

}
