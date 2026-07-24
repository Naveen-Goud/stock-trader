package com.trading.trading.client;

import com.trading.trading.dto.WalletBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/internal/{userId}/wallet")
    WalletBalanceResponse getWalletBalance(@PathVariable("userId") Long userId);
}
