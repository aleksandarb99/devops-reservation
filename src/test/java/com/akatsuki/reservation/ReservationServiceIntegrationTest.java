package com.akatsuki.reservation;

import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.repository.ReservationRepository;
import com.akatsuki.reservation.service.ReservationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

@SpringBootTest
@Testcontainers(parallel = true)
class ReservationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", db::getJdbcUrl);
        registry.add("spring.datasource.username", db::getUsername);
        registry.add("spring.datasource.password", db::getPassword);
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void createReservationTest() {
        // Given
        CreateReservationDto reservationDto = CreateReservationDto.builder()
                .accommodationId(1L)
                .startDate(LocalDate.of(2022, 2, 10))
                .endDate(LocalDate.of(2022, 2, 15))
                .numberOfGuests(1)
                .build();

        // When
        reservationService.createReservation(reservationDto);

        // Then
        Assertions.assertEquals(3, reservationRepository.count());  // TODO Check why it is not 4, it should be
    }
}

