package com.akatsuki.reservation.feignclients;

import com.akatsuki.reservation.dto.AccommodationCheckDto;
import com.akatsuki.reservation.dto.AvailabilityCheckResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(url = "${core.services.accommodation-url}", value = "accommodation-feign-client")
public interface AccommodationFeignClient {

    @PostMapping("/{id}/check-availability")
    AvailabilityCheckResponseDto checkAccommodationAvailability(@PathVariable Long id, @RequestBody AccommodationCheckDto accommodationCheckDto);
}
