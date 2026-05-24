package com.vbforge.ras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * JUNIOR NOTE — @SpringBootApplication is a convenience annotation that combines:
 * - @Configuration: this class can define @Bean methods
 * - @EnableAutoConfiguration: Spring Boot auto-configures beans based on classpath
 * - @ComponentScan: scans this package and sub-packages for @Service, @Controller, etc.
 *
 * Everything in com.vbforge.ras.** is auto-discovered. No XML config needed.
 */
@SpringBootApplication
public class RasApplication {

    public static void main(String[] args) {
        SpringApplication.run(RasApplication.class, args);
    }
}
