package com.akatsuki.reservation.config;

import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.model.Reservation;
import com.akatsuki.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DbInitializationStartupRunner implements ApplicationRunner {

    private final ReservationRepository reservationRepository;

    @Override
    public void run(ApplicationArguments args) {
        var r1 = Reservation.builder()
                .id(UUID.randomUUID().toString())
                .accommodationId(1L)
                .hostId(1L)
                .guestId(3L)
                .startDate(LocalDate.of(2023, 5, 10))
                .endDate(LocalDate.of(2023, 5, 15))
                .status(ReservationStatus.REQUESTED)
                .numberOfGuests(3)
                .build();
        var r2 = Reservation.builder()
                .id(UUID.randomUUID().toString())
                .accommodationId(1L)
                .hostId(1L)
                .guestId(3L)
                .startDate(LocalDate.of(2023, 5, 10))
                .endDate(LocalDate.of(2023, 5, 15))
                .status(ReservationStatus.CANCELLED)
                .numberOfGuests(4)
                .build();
        var r3 = Reservation.builder()
                .id(UUID.randomUUID().toString())
                .accommodationId(2L)
                .hostId(1L)
                .guestId(3L)
                .startDate(LocalDate.of(2023, 6, 5))
                .endDate(LocalDate.of(2023, 6, 15))
                .status(ReservationStatus.REQUESTED)
                .numberOfGuests(2)
                .build();
        reservationRepository.saveAll(List.of(r1, r2, r3));
    }
}
