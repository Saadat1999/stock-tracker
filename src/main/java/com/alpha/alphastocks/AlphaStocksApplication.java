package com.alpha.alphastocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class AlphaStocksApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlphaStocksApplication.class, args);
    }

}
