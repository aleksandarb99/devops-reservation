package com.akatsuki.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccommodationCheckDto {
    private int numberOfGuests;
    private LocalDate startDate;
    private LocalDate endDate;
}
