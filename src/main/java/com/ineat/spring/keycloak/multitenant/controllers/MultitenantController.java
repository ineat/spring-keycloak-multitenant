package com.ineat.spring.keycloak.multitenant.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
public class MultitenantController {

    @RequestMapping(name = "/admin",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String adminSecuredEndpoint(){
        return "Documents admin";
    }
 
    @RequestMapping("/user")
    public String userSecuredEndpoint(){
        return "Documents user";
    }
}