package com.akatsuki.reservation.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${core.services.user-url}", value = "user-feign-client")
public interface UserFeignClient {

    @PutMapping("/cancellation")
    void addCancellation(@RequestHeader("Authorization") final String token);

}
