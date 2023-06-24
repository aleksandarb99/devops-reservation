package com.akatsuki.reservation.feignclients;

import com.akatsuki.reservation.dto.AccommodationBasicsDto;
import com.akatsuki.reservation.dto.AccommodationCheckDto;
import com.akatsuki.reservation.dto.AvailabilityCheckResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(url = "${core.services.accommodation-url}", value = "accommodation-feign-client")
public interface AccommodationFeignClient {

    @PostMapping("/{id}/check-availability")
    AvailabilityCheckResponseDto checkAccommodationAvailability(@RequestHeader("Authorization") final String token, @PathVariable Long id, @RequestBody AccommodationCheckDto accommodationCheckDto);

    @GetMapping("/per-host")
    List<AccommodationBasicsDto> findPerHostAccommodations(@RequestHeader("Authorization") final String token);
}
