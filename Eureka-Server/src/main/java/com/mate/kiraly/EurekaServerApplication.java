package com.mate.kiraly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication { // Call other service like: REST template https://SERVICE-NAME/path/to/api also @LoadBalanced
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}