package com.akatsuki.reservation.service.impl;

import com.akatsuki.reservation.dto.ReservationDto;
import com.akatsuki.reservation.model.Reservation;
import com.akatsuki.reservation.repository.ReservationRepository;
import com.akatsuki.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    @Override
    public void createReservation(ReservationDto reservationDto) {
        // TODO Check if some extra validation is needed
        Reservation reservation = modelMapper.map(reservationDto, Reservation.class);
        reservationRepository.save(reservation);
    }
}
