package com.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Author mrliz
 * @Date 2020/12/8 16:02
 */
@EnableEurekaServer
@SpringBootApplication
public class MicroservicecloudEureka7002 {
    public static void main(String[] args) {
        SpringApplication.run(MicroservicecloudEureka7002.class,args);
    }
}
