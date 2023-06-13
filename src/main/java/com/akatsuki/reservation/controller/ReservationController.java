package com.akatsuki.reservation.controller;

import com.akatsuki.reservation.dto.AccommodationInfoDTO;
import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.dto.ReservationDetailsDTO;
import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.model.Reservation;
import com.akatsuki.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reservation")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReservation(@RequestBody CreateReservationDto createReservationDto) { // TODO Add @Valid if DTO is validated
        reservationService.createReservation(createReservationDto);
    }

    @PutMapping("/cancel/{reservationId}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelReservation(@PathVariable("reservationId") String reservationId) {
        reservationService.cancelReservation(reservationId);
    }

    @GetMapping("/by-user-and-status/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationDetailsDTO> getReservations(@RequestParam(name = "status", required = false) ReservationStatus status,
                                                       @PathVariable("userId") Long userId) {    // TODO Take it from token
        return reservationService.getReservations(status, userId);
    }

    // TODO Check this one, once you integrate with Auth
    @GetMapping("/by-accommodation-and-status/{accommodationId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationDetailsDTO> getReservationsByAccommodation(@RequestParam(name = "status", required = false) ReservationStatus status,
                                                       @PathVariable("accommodationId") Long accommodationId) {
        return reservationService.getReservationsByAccommodation(status, accommodationId);
    }

    @PutMapping("/deny/{reservationId}")
    @ResponseStatus(HttpStatus.OK)
    public void denyReservationRequest(@PathVariable("reservationId") String reservationId) {
        reservationService.denyReservation(reservationId);
    }

    @PutMapping("/approve/{reservationId}")
    @ResponseStatus(HttpStatus.OK)
    public void approveReservationRequest(@PathVariable("reservationId") String reservationId) {
        reservationService.approveReservation(reservationId);
    }

    @GetMapping("/check-reservations-of-accommodation")
    @ResponseStatus(HttpStatus.OK)
    public boolean checkReservationsOfAccommodation(@RequestBody AccommodationInfoDTO accommodationInfoDTO) {
        return reservationService.checkReservationsOfAccommodation(accommodationInfoDTO);
    }

    @GetMapping("/check-host-reservations/{hostId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean checkIfHostCanBeDeleted(@PathVariable("hostId") Long hostId) {   // TODO Take it from token
        return reservationService.checkIfHostCanBeDeleted(hostId);
    }

    @GetMapping("/check-guest-reservations/{guestId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean checkIfGuestCanBeDeleted(@PathVariable("guestId") Long guestId) {   // TODO Take it from token
        return reservationService.checkIfGuestCanBeDeleted(guestId);
    }
}
