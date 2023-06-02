package com.akatsuki.reservation.dto;

import com.akatsuki.reservation.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private Long accommodationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfGuests;
    private ReservationStatus status;
    // TODO Check if some validation is needed
}
