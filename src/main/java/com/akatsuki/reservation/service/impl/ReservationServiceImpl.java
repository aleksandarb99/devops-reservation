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
    public void createReservation(CreateReservationDto createReservationDto, Long guestId, String token) {

        AvailabilityCheckResponseDto availabilityCheckResponseDto = checkAccommodationAvailability(createReservationDto, token);

        if (!availabilityCheckResponseDto.isAvailable()) {
            throw new BadRequestException("It is not possible to create reservation for requested accommodation and date!");
        }

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

        if (availabilityCheckResponseDto.isAutomaticApprove()) {
            reservation.setStatus(ReservationStatus.APPROVED);
        } else {
            reservation.setStatus(ReservationStatus.REQUESTED);
        }

        reservation.setGuestId(guestId);
        reservation.setHostId(availabilityCheckResponseDto.getHostId());
        reservation.setTotalPrice(availabilityCheckResponseDto.getTotalCost());
        reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(String reservationId, String token) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BadRequestException("There's no such reservation present with given id " + reservationId));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {

            if (!LocalDate.now().isBefore(reservation.getStartDate())) {
                throw new BadRequestException("All up to one day before reservation check in, you are able to cancel it!");
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        userFeignClient.addCancellation(token);
    }

    @Override
    public List<ReservationDetailsDTO> getReservations(ReservationStatus status, Long id) {
        List<Reservation> reservations;
        if (status == null) {
            if (status.equals(ReservationStatus.REQUESTED)) {
                reservations = reservationRepository.findAllByHostId(id);
            } else {
                reservations = reservationRepository.findAllByGuestId(id);
            }
            return mapToDtos(reservations);
        }

        if (status.equals(ReservationStatus.REQUESTED)) {
            reservations = reservationRepository.findAllByHostIdAndStatus(id, status);
        } else {
            reservations = reservationRepository.findAllByGuestIdAndStatus(id, status);
        }
        return mapToDtos(reservations);
    }

    @Override
    public List<ReservationDetailsDTO> getReservationsByGuest(ReservationStatus status, Long guestId) {
        if (status == null) {
            List<Reservation> reservations = reservationRepository.findAllByGuestId(guestId);
            return mapToDtos(reservations);
        }

        List<Reservation> reservations = reservationRepository.findAllByGuestIdAndStatus(guestId, status);
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
            userIds.add(reservation.getGuestId());
        }

        // TODO From auth service fetch details for users using userIds
        return mapToDtosAndAttachUsers(reservations, new HashMap<>());
    }

    @Override
    public void denyReservation(String reservationId, Long hostId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BadRequestException("There's no such reservation present with given id " + reservationId));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new BadRequestException("Can not be denied! It is already approved!");
        }

        if (!reservation.getHostId().equals(hostId)) {
            throw new BadRequestException("Can not be denied! You are not correct host!");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public void approveReservation(String reservationId, Long hostId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BadRequestException("There's no such reservation present with given id " + reservationId));

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new BadRequestException("Can not be approved! It is cancelled!");
        }

        if (!reservation.getHostId().equals(hostId)) {
            throw new BadRequestException("Can not be denied! You are not correct host!");
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
    public boolean checkReservationsOfAccommodation(Long accommodationId, LocalDate startDate, LocalDate endDate) {
        List<Reservation> reservations = reservationRepository.findAllByAccommodationIdAndStatusNot(
                accommodationId, ReservationStatus.CANCELLED);

        for (Reservation reservation : reservations) {
            if (reservation.getStartDate().isAfter(startDate) && reservation.getStartDate().isBefore(endDate)) {
                return false;
            }
            if (startDate.isAfter(reservation.getStartDate()) && startDate.isBefore(reservation.getEndDate())) {
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
        List<Reservation> reservations = reservationRepository.findAllByHostIdAndStatusAndStartDateAfter(
                guestId, ReservationStatus.APPROVED, LocalDate.now());
        return reservations.size() == 0;
    }

    private List<ReservationDetailsDTO> mapToDtosAndAttachUsers(List<Reservation> reservations, Map<Long, UserDetailsDTO> userDetailsDTOMap) {
        return reservations.stream().map(reservation -> {
            ReservationDetailsDTO reservationDetailsDTO = modelMapper.map(reservation, ReservationDetailsDTO.class);
            reservationDetailsDTO.setUser(userDetailsDTOMap.get(reservation.getGuestId()));
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
