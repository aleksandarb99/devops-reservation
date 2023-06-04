package com.akatsuki.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationInfoDTO {
    private Long accommodationId;
    private LocalDate startDate;
    private LocalDate endDate;
}
