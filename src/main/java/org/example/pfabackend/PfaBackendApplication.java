package org.example.pfabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "org.example.pfabackend")
public class PfaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PfaBackendApplication.class, args);
    }

}
