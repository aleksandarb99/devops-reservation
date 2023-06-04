package com.akatsuki.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private int numberOfCancellations;
    // TODO Align with data from auth service
}
