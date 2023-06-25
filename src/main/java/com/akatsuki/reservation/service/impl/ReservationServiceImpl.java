package com.akatsuki.reservation.service.impl;

import com.akatsuki.reservation.dto.*;
import com.akatsuki.reservation.enums.ReservationStatus;
import com.akatsuki.reservation.exception.BadRequestException;
import com.akatsuki.reservation.feignclients.AccommodationFeignClient;
import com.akatsuki.reservation.feignclients.UserFeignClient;
import com.akatsuki.reservation.model.Reservation;
import com.akatsuki.reservation.repository.ReservationRepository;
import com.akatsuki.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    private final AccommodationFeignClient accommodationFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public void createReservation(CreateReservationDto createReservationDto, String token) {
        AvailabilityCheckResponseDto availabilityCheckResponseDto = checkAccommodationAvailability(createReservationDto, token);
        // Check if accommodation is available for chosen dates
        if (!availabilityCheckResponseDto.isAvailable()) {
            throw new BadRequestException("It is not possible to create reservation for requested accommodation and date!");
        }

        // Fetch reservations for chosen accommodation
        List<Reservation> approvedReservations = reservationRepository.findAllByAccommodationIdAndStatus(
                createReservationDto.getAccommodationId(), ReservationStatus.APPROVED);

        for (Reservation approvedReservation : approvedReservations) {
            if (createReservationDto.getStartDate().isAfter(approvedReservation.getStartDate()) && createReservationDto.getStartDate().isBefore(approvedReservation.getEndDate())) {
                throw new BadRequestException("Selected date range is overlapping with existing reservations!");
            }
            if (approvedReservation.getStartDate().isAfter(createReservationDto.getStartDate()) && approvedReservation.getStartDate().isBefore(createReservationDto.getEndDate())) {
                throw new BadRequestException("Selected date range is overlapping with existing reservations!");
            }
        }

        Reservation reservation = modelMapper.map(createReservationDto, Reservation.class);
        // If it needs manual approval, save it as a request
        if (availabilityCheckResponseDto.isAutomaticApprove()) {
            reservation.setStatus(ReservationStatus.APPROVED);
        } else {
            reservation.setStatus(ReservationStatus.REQUESTED);
        }

        reservation.setTotalPrice(availabilityCheckResponseDto.getTotalCost());
        reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(String reservationId, String token) {
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

        userFeignClient.addCancellation(token, reservation.getUserId());
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
    public List<ReservationDetailsDTO> getReservationsByGuest(ReservationStatus status, Long guestId) {
        if (status == null) {
            List<Reservation> reservations = reservationRepository.findAllByGuestId(guestId);
            return mapToDtos(reservations);
        }

        List<Reservation> reservations = reservationRepository.findAllByUserIdAndStatus(guestId, status);
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
    public void denyReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BadRequestException("There's no such reservation present with given id " + reservationId));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new BadRequestException("Can not be denied! It is already approved!");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public void approveReservation(String reservationId) {
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

    @Override
    public boolean checkIfHostCanBeDeleted(String token) {
        // TODO Check if we need to return whole new DTO, all we need is collection of accommodation ids
        List<AccommodationBasicsDto> hostAccommodations = accommodationFeignClient.findPerHostAccommodations(token);

        for (AccommodationBasicsDto accommodationBasicDto : hostAccommodations) {
            List<Reservation> reservations = reservationRepository.findAllByAccommodationIdAndStatusAndStartDateAfter(
                    accommodationBasicDto.getId(), ReservationStatus.APPROVED, LocalDate.now());
            if (reservations.size() > 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean checkIfGuestCanBeDeleted(Long guestId) {
        List<Reservation> reservations = reservationRepository.findAllByUserIdAndStatusAndStartDateAfter(
                guestId, ReservationStatus.APPROVED, LocalDate.now());
        return reservations.size() == 0;
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

    private AvailabilityCheckResponseDto checkAccommodationAvailability(CreateReservationDto createReservationDto, String token) {
        AccommodationCheckDto accommodationCheckDto = AccommodationCheckDto.builder()
                .startDate(createReservationDto.getStartDate())
                .endDate(createReservationDto.getEndDate())
                .numberOfGuests(createReservationDto.getNumberOfGuests())
                .build();

        return accommodationFeignClient.checkAccommodationAvailability(token,
                createReservationDto.getAccommodationId(), accommodationCheckDto);
    }
}
