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
public class ReservationDetailsDTO {
    private String id;
    private Long accommodationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfGuests;
    private ReservationStatus status;
    private double totalPrice;
    private UserDetailsDTO user;
}
