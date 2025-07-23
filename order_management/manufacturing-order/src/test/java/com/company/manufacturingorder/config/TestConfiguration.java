package com.company.manufacturingorder.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "com.company.manufacturingorder",
    "com.company.sharedkernel"
})
public class TestConfiguration {
}