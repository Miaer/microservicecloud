package com.web.service.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

/**
 * @description: 用户服务
 * @author: LZG
 * @create: 2021-01-18 10:32
 **/
@WebService
public class UserService {

    @WebMethod
    public String getUser(Long id){
        return "用户ID是：" + id;
    }

    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8090/service/UserService",new UserService());
        System.out.println("服务发布成功");
    }
}
