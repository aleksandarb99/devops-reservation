package com.akatsuki.reservation.dto;

import com.akatsuki.reservation.enums.PriceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AccommodationBasicsDto {
    private Long id;
    private String name;
    private Long hostId;
    private boolean automaticApprove;
    private String location;
    private int minQuests;
    private int maxQuests;
    private PriceType priceType;
    private int defaultPrice;
}
