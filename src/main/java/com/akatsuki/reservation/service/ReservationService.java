package com.akatsuki.reservation.service;

import com.akatsuki.reservation.dto.ReservationDto;

public interface ReservationService {
    void createReservation(ReservationDto reservationDto);
}
