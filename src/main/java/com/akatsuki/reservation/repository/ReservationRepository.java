package com.akatsuki.reservation.repository;

import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.model.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {

    List<Reservation> findAllByHostIdAndStatusAndStartDateAfter(Long userId, ReservationStatus status, LocalDate currentDate);

    List<Reservation> findAllByGuestId(Long guestId);

    List<Reservation> findAllByAccommodationId(Long accommodationId);

    List<Reservation> findAllByAccommodationIdAndStatus(Long accommodationId, ReservationStatus status);

    List<Reservation> findAllByAccommodationIdAndStatusNot(Long accommodationId, ReservationStatus status);

    List<Reservation> findAllByAccommodationIdAndStatusAndStartDateAfter(Long accommodationId, ReservationStatus status, LocalDate currentDate);

    List<Reservation> findAllByHostId(Long hostId);

    List<Reservation> findAllByHostIdAndStatus(Long hostId, ReservationStatus status);

    List<Reservation> findAllByGuestIdAndStatus(Long guestId, ReservationStatus status);
}