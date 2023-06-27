package com.akatsuki.reservation.model;

import com.akatsuki.reservation.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Reservation {
    @Id
    private String id;
    private Long accommodationId;
    private Long userId;
    private Long guestId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfGuests;
    private ReservationStatus status;
    private double totalPrice;
}
