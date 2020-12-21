package com.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: LZG
 * @create: 2020-12-21 14:41
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class MicroServiceCloudProviderNacos8081 {
    public static void main(String[] args) {
        SpringApplication.run(MicroServiceCloudProviderNacos8081.class, args);
    }

    @RestController
    public class EchoController {
        @GetMapping(value = "/echo/{string}")
        public String echo(@PathVariable String string) {
            return "Hello Nacos Discovery " + string;
        }
    }
}
