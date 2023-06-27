package com.akatsuki.reservation.service;

import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.dto.ReservationDetailsDTO;
import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    List<Reservation> getAllReservations();

    List<ReservationDetailsDTO> getReservations(ReservationStatus status, Long userId);

    List<ReservationDetailsDTO> getReservationsByGuest(ReservationStatus status, Long guestId);


    List<ReservationDetailsDTO> getReservationsByAccommodation(ReservationStatus status, Long accommodationId);

    boolean checkReservationsOfAccommodation(Long id, LocalDate startDate, LocalDate endDate);

    void createReservation(CreateReservationDto reservationDto, Long guestId, String token);

    void cancelReservation(String reservationId, String token);

    void denyReservation(String reservationId, Long hostId);

    void approveReservation(String reservationId, Long hostId);

    boolean checkIfHostCanBeDeleted(String token);

    boolean checkIfGuestCanBeDeleted(Long guestId);

}
