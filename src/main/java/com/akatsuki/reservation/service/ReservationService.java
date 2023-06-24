package com.akatsuki.reservation.service;

import com.akatsuki.reservation.dto.AccommodationInfoDTO;
import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.dto.ReservationDetailsDTO;
import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.model.Reservation;

import java.util.List;

public interface ReservationService {
    List<Reservation> getAllReservations();

    List<ReservationDetailsDTO> getReservations(ReservationStatus status, Long userId);

    List<ReservationDetailsDTO> getReservationsByAccommodation(ReservationStatus status, Long accommodationId);

    boolean checkReservationsOfAccommodation(AccommodationInfoDTO accommodationInfoDTO);

    void createReservation(CreateReservationDto reservationDto, String token);

    void cancelReservation(String reservationId, String token);

    void denyReservation(String reservationId);

    void approveReservation(String reservationId);

    boolean checkIfHostCanBeDeleted(String token);

    boolean checkIfGuestCanBeDeleted(Long guestId);
}
