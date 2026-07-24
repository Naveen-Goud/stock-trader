package com.trading.user.repository;

import com.trading.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsername(@Param("username") String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    boolean existsByUsername(@Param("username") String username);

    boolean existsByEmail(String email);

    /**
     * Atomic, race-safe debit: only succeeds if the balance is still
     * sufficient at the moment the UPDATE runs, so two concurrent trades
     * can never both pass a stale in-memory balance check and overdraw
     * the wallet. Returns the number of rows updated (0 or 1).
     */
    @Modifying
    @Query("UPDATE User u SET u.walletBalance = u.walletBalance - :amount " +
           "WHERE u.id = :userId AND u.walletBalance >= :amount")
    int debitWallet(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE User u SET u.walletBalance = u.walletBalance + :amount WHERE u.id = :userId")
    int creditWallet(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
