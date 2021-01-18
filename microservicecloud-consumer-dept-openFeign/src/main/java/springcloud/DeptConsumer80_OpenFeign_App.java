package springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author mrliz
 * @Date 2020/12/9 21:24
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.springcloud"})
public class DeptConsumer80_OpenFeign_App {

    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80_OpenFeign_App.class,args);
    }
}
