package com.akatsuki.reservation;

import com.akatsuki.reservation.dto.CreateReservationDto;
import com.akatsuki.reservation.model.Reservation;
import com.akatsuki.reservation.repository.ReservationRepository;
import com.akatsuki.reservation.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepositoryMock;
    @Mock
    private ModelMapper modelMapperMock;
    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void createReservationTest() {
        // Given
        CreateReservationDto reservationDto = new CreateReservationDto();
        Reservation reservation = new Reservation();

        when(modelMapperMock.map(any(CreateReservationDto.class), eq(Reservation.class))).thenReturn(reservation);

        // When
        reservationService.createReservation(reservationDto);

        // Then
        verify(reservationRepositoryMock, times(1)).save(any(Reservation.class));
    }
}
