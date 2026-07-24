package com.trading.user.controller;

import com.trading.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Internal-only endpoint used by Trading Service to validate wallet balance
 * before executing a BUY. Never exposed through the public API Gateway --
 * see JwtGatewayFilter, which blocks any path containing "/internal/".
 */
@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalWalletController {

    private final UserRepository userRepository;

    public record WalletBalanceResponse(Long userId, BigDecimal walletBalance) {}

    @GetMapping("/{userId}/wallet")
    public ResponseEntity<WalletBalanceResponse> getWalletBalance(@PathVariable Long userId) {
        BigDecimal balance = userRepository.findById(userId)
                .map(com.trading.user.entity.User::getWalletBalance)
                .orElse(BigDecimal.ZERO);
        return ResponseEntity.ok(new WalletBalanceResponse(userId, balance));
    }
}
