package com.akatsuki.reservation.service.impl;

import com.akatsuki.reservation.dto.AccommodationInfoDTO;
import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.dto.ReservationDetailsDTO;
import com.akatsuki.reservation.dto.UserDetailsDTO;
import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.exception.BadRequestException;
import com.akatsuki.reservation.model.Reservation;
import com.akatsuki.reservation.repository.ReservationRepository;
import com.akatsuki.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    @Override
    public void createReservation(CreateReservationDto reservationDto) {
        // Check if accommodation is available for chosen dates
        boolean isAvailable = true;         // TODO Check availability for chosen reservation, and calculate current price
        if (!isAvailable) {
            throw new BadRequestException("It is not possible to create reservation for requested accommodation and date!");
        }
        Reservation reservation = modelMapper.map(reservationDto, Reservation.class);
//        reservation.setTotalPrice(response.getTotalPrice());
        // Fetch reservations for chosen accommodation
        List<Reservation> approvedReservations = reservationRepository.findAllByAccommodationIdAndStatus(
                reservation.getAccommodationId(), ReservationStatus.APPROVED);

        for (Reservation approvedReservation : approvedReservations) {
            if (reservation.getStartDate().isAfter(approvedReservation.getStartDate()) && reservation.getStartDate().isBefore(approvedReservation.getEndDate())) {
                throw new BadRequestException("Selected date range is overlapping with existing reservations!");
            }
            if (approvedReservation.getStartDate().isAfter(reservation.getStartDate()) && approvedReservation.getStartDate().isBefore(reservation.getEndDate())) {
                throw new BadRequestException("Selected date range is overlapping with existing reservations!");
            }
        }

        boolean isAutomatic = false;    // TODO Get from response
        // If it needs manual approval, save it as a request
        if (isAutomatic) {
            reservation.setStatus(ReservationStatus.APPROVED);
        } else {
            reservation.setStatus(ReservationStatus.REQUESTED);
        }

        reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BadRequestException("There's no such reservation present with given id " + reservationId));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            long differenceInMinutes = ChronoUnit.MINUTES.between(reservation.getStartDate(), LocalDateTime.now());
            if (differenceInMinutes > 1440) {
                throw new BadRequestException("All up to one day before reservation check in, you are able to cancel it!");
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // TODO Call auth service to increase number of cancelled reservations by 1
    }

    @Override
    public List<ReservationDetailsDTO> getReservations(ReservationStatus status, Long userId) {
        if (status == null) {
            List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
            return mapToDtos(reservations);
        }

        List<Reservation> reservations = reservationRepository.findAllByUserIdAndStatus(userId, status);
        return mapToDtos(reservations);
    }

    @Override
    public List<ReservationDetailsDTO> getReservationsByAccommodation(ReservationStatus status, Long accommodationId) {
        List<Reservation> reservations;
        if (status == null) {
            reservations = reservationRepository.findAllByAccommodationId(accommodationId);
        } else {
            reservations = reservationRepository.findAllByAccommodationIdAndStatus(accommodationId, status);
        }

        List<ReservationDetailsDTO> reservationDetailsDTOS = mapToDtos(reservations);

        Set<Long> userIds = new HashSet<>();
        for (Reservation reservation : reservations) {
            userIds.add(reservation.getUserId());
        }

        // TODO From auth service fetch details for users using userIds
        return mapToDtosAndAttachUsers(reservations, new HashMap<>());
    }

    @Override
    public void denyReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BadRequestException("There's no such reservation present with given id " + reservationId));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new BadRequestException("Can not be denied! It is already approved!");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public void approveReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BadRequestException("There's no such reservation present with given id " + reservationId));

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new BadRequestException("Can not be approved! It is cancelled!");
        }

        List<Reservation> requestedReservations = reservationRepository.findAllByAccommodationIdAndStatus(
                reservation.getAccommodationId(), ReservationStatus.REQUESTED);

        for (Reservation requestedReservation : requestedReservations) {
            if (reservation.getStartDate().isAfter(requestedReservation.getStartDate()) && reservation.getStartDate().isBefore(requestedReservation.getEndDate())) {
                requestedReservation.setStatus(ReservationStatus.CANCELLED);
            }
            if (requestedReservation.getStartDate().isAfter(reservation.getStartDate()) && requestedReservation.getStartDate().isBefore(reservation.getEndDate())) {
                requestedReservation.setStatus(ReservationStatus.CANCELLED);
            }
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        requestedReservations.add(reservation);
        reservationRepository.saveAll(requestedReservations);
    }

    @Override
    public boolean checkReservationsOfAccommodation(AccommodationInfoDTO accommodationInfoDTO) {
        List<Reservation> reservations = reservationRepository.findAllByAccommodationIdAndStatusNot(
                accommodationInfoDTO.getAccommodationId(), ReservationStatus.CANCELLED);

        for (Reservation reservation : reservations) {
            if (reservation.getStartDate().isAfter(accommodationInfoDTO.getStartDate()) && reservation.getStartDate().isBefore(accommodationInfoDTO.getEndDate())) {
                return false;
            }
            if (accommodationInfoDTO.getStartDate().isAfter(reservation.getStartDate()) && accommodationInfoDTO.getStartDate().isBefore(reservation.getEndDate())) {
                return false;
            }
        }

        return true;
    }

    private List<ReservationDetailsDTO> mapToDtosAndAttachUsers(List<Reservation> reservations, Map<Long, UserDetailsDTO> userDetailsDTOMap) {
        return reservations.stream().map(reservation -> {
            ReservationDetailsDTO reservationDetailsDTO = modelMapper.map(reservation, ReservationDetailsDTO.class);
            reservationDetailsDTO.setUser(userDetailsDTOMap.get(reservation.getUserId()));
            return reservationDetailsDTO;
        }).toList();
    }

    private List<ReservationDetailsDTO> mapToDtos(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> modelMapper.map(reservation, ReservationDetailsDTO.class))
                .toList();
    }
}
