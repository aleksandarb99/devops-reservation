package com.akatsuki.reservation.service;

import com.akatsuki.reservation.dto.AccommodationInfoDTO;
import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.dto.ReservationDetailsDTO;
import com.akatsuki.reservation.enums.ReservationStatus;

import java.util.List;

public interface ReservationService {
    void createReservation(CreateReservationDto reservationDto);

    void cancelReservation(Long reservationId);

    List<ReservationDetailsDTO> getReservations(ReservationStatus status, Long userId);

    List<ReservationDetailsDTO> getReservationsByAccommodation(ReservationStatus status, Long accommodationId);

    void denyReservation(Long reservationId);

    void approveReservation(Long reservationId);

    boolean checkReservationsOfAccommodation(AccommodationInfoDTO accommodationInfoDTO);
}
