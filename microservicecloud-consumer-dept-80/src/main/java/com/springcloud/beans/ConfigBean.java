package com.springcloud.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author mrliz
 * @Date 2020/12/9 21:23
 */
@Configuration
public class ConfigBean {

    @Bean
    public RestTemplate getRestTemplate()
    {
        return new RestTemplate();
    }
}
