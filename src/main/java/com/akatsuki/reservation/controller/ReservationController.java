package com.akatsuki.reservation.controller;

import com.akatsuki.reservation.dto.ReservationDto;
import com.akatsuki.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reservation")
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReservation(@RequestBody ReservationDto reservationDto) { // TODO Add @Valid if DTO is validated
        reservationService.createReservation(reservationDto);
    }
}
