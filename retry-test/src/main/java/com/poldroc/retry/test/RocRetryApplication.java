package com.poldroc.retry.test;

import com.poldroc.retry.spring.annotation.EnableRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableRetry
@SpringBootApplication
@ComponentScan(basePackages = "com.poldroc.retry.test.service")
public class RocRetryApplication {
    public static void main(String[] args) {
        SpringApplication.run(RocRetryApplication.class, args);
    }
}
