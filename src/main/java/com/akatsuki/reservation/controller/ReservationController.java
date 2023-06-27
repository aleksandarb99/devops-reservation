package com.akatsuki.reservation.controller;

import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.dto.ReservationDetailsDTO;
import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final JwtDecoder jwtDecoder;

    @GetMapping("/requested")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationDetailsDTO> getRequestedReservations(@RequestHeader("Authorization") final String token) {
        Long hostId = getIdFromToken(token);
        return reservationService.getReservations(ReservationStatus.REQUESTED, hostId);
    }

    @GetMapping("/approved")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationDetailsDTO> getCurrentReservations(@RequestHeader("Authorization") final String token) {
        Long guestId = getIdFromToken(token);
        return reservationService.getReservations(ReservationStatus.APPROVED, guestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReservation(@RequestBody CreateReservationDto createReservationDto, @RequestHeader("Authorization") final String token) { // TODO Add @Valid if DTO is validated
        Long guestId = getIdFromToken(token);
        reservationService.createReservation(createReservationDto, guestId, token);
    }

    @PutMapping("/cancel/{reservationId}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelReservation(@PathVariable("reservationId") String reservationId, @RequestHeader("Authorization") final String token) {
        reservationService.cancelReservation(reservationId, token);
    }


    //    TODO: Who call this? Check this
    // TODO Check this one, once you integrate with Auth
    @GetMapping("/by-accommodation-and-status/{accommodationId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationDetailsDTO> getReservationsByAccommodation(@RequestParam(name = "status", required = false) ReservationStatus status,
                                                                      @PathVariable("accommodationId") Long accommodationId) {
        return reservationService.getReservationsByAccommodation(status, accommodationId);
    }

    @PutMapping("/deny/{reservationId}")
    @ResponseStatus(HttpStatus.OK)
    public void denyReservationRequest(@PathVariable("reservationId") String reservationId, @RequestHeader("Authorization") final String token) {
        Long hostId = getIdFromToken(token);
        reservationService.denyReservation(reservationId, hostId);
    }

    @PutMapping("/approve/{reservationId}")
    @ResponseStatus(HttpStatus.OK)
    public void approveReservationRequest(@PathVariable("reservationId") String reservationId, @RequestHeader("Authorization") final String token) {
        Long hostId = getIdFromToken(token);
        reservationService.approveReservation(reservationId, hostId);
    }

    @GetMapping("/check-reservations-of-accommodation")
    @ResponseStatus(HttpStatus.OK)
    public boolean checkReservationsOfAccommodation(@RequestParam(name = "id", required = true) Long id,
                                                    @RequestParam(name = "startDate", required = true) LocalDate startDate,
                                                    @RequestParam(name = "endDate", required = true) LocalDate endDate) {
        return reservationService.checkReservationsOfAccommodation(id, startDate, endDate);
    }

    @GetMapping("/check-host-reservations")
    @ResponseStatus(HttpStatus.OK)
    public boolean checkIfHostCanBeDeleted(@RequestHeader("Authorization") final String token) {
        return reservationService.checkIfHostCanBeDeleted(token);
    }

    @GetMapping("/check-guest-reservations")
    @ResponseStatus(HttpStatus.OK)
    public boolean checkIfGuestCanBeDeleted(@RequestHeader("Authorization") final String token) {
        Long guestId = getIdFromToken(token);
        return reservationService.checkIfGuestCanBeDeleted(guestId);
    }

    private Long getIdFromToken(String token) {
        return (Long) jwtDecoder.decode(token.split(" ")[1]).getClaims().get("id");
    }

}
