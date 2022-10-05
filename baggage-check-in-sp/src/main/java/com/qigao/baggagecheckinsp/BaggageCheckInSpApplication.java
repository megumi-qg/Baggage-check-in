package com.qigao.baggagecheckinsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class BaggageCheckInSpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaggageCheckInSpApplication.class, args);
    }

}
