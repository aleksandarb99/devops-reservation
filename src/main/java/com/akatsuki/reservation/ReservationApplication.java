package com.akatsuki.reservation;

import com.akatsuki.reservation.feignclients.AccommodationFeignClient;
import com.akatsuki.reservation.feignclients.UserFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableFeignClients(clients = {AccommodationFeignClient.class, UserFeignClient.class})
@EnableMongoRepositories
public class ReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

}
