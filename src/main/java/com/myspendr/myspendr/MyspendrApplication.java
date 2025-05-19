package com.myspendr.myspendr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.myspendr.myspendr")
public class MyspendrApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyspendrApplication.class, args);
    }
}
