package com.akatsuki.reservation;

import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.model.Reservation;
import com.akatsuki.reservation.repository.ReservationRepository;
import com.akatsuki.reservation.service.ReservationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@Testcontainers(parallel = true)
class ReservationServiceIntegrationTest {

    @Container
    static MongoDBContainer db = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", db::getReplicaSetUrl);
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void denyReservationTest() {
        List<Reservation> reservationList = reservationRepository.findAll().stream().filter(
                r -> r.getStatus().equals(ReservationStatus.REQUESTED)).toList();
        String id = reservationList.get(0).getId();

        reservationService.denyReservation(id);
        Optional<Reservation> reservationOptional = reservationRepository.findById(id);
        Assertions.assertTrue(reservationOptional.isPresent());
        Reservation reservation = reservationOptional.get();
        Assertions.assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }
}

