package ru.naumen.ectmapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("ru.naumen.ectmapi.config")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
