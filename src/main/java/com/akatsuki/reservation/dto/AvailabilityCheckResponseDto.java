package com.akatsuki.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityCheckResponseDto {
    private Long id;
    private Long hostId;
    private boolean available;
    private int totalCost;
    private boolean automaticApprove;
}
