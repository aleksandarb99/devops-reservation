package com.akatsuki.reservation.feignclients;

import com.akatsuki.reservation.dto.AccommodationBasicsDto;
import com.akatsuki.reservation.dto.AccommodationCheckDto;
import com.akatsuki.reservation.dto.AvailabilityCheckResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(url = "${core.services.accommodation-url}", value = "accommodation-feign-client")
public interface AccommodationFeignClient {

    @PostMapping("/{id}/check-availability")
    AvailabilityCheckResponseDto checkAccommodationAvailability(@PathVariable Long id, @RequestBody AccommodationCheckDto accommodationCheckDto);

    //    TODO: Use this
    @GetMapping("/per-host/{id}")
    List<AccommodationBasicsDto> findPerHostAccommodations(@PathVariable Long id);
}
