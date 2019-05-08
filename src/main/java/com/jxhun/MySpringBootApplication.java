package com.jxhun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Created with IntelliJ IDEA.
 * User: thinknovo
 * Date: 2018/06/21
 * Description:
 * Version: V1.0
 */
@SpringBootApplication
public class MySpringBootApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MySpringBootApplication.class);
    }

    public static void main(String[] args){
        SpringApplication.run(MySpringBootApplication.class, args);
    }

}

