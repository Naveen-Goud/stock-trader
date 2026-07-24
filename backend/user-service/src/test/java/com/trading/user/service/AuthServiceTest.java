package com.trading.user.service;

import com.trading.user.dto.*;
import com.trading.user.entity.RefreshToken;
import com.trading.user.entity.User;
import com.trading.user.exception.InvalidCredentialsException;
import com.trading.user.exception.InvalidTokenException;
import com.trading.user.exception.UserAlreadyExistsException;
import com.trading.user.mapper.UserMapper;
import com.trading.user.repository.RefreshTokenRepository;
import com.trading.user.repository.UserRepository;
import com.trading.user.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("naveen").email("naveen@example.com")
                .passwordHash("hashed").role(User.Role.USER).build();
    }

    @Test
    void register_success_savesUserAndReturnsResponse() {
        RegisterRequest request = new RegisterRequest("naveen", "naveen@example.com", "Pass@1234");

        when(userRepository.existsByUsername("naveen")).thenReturn(false);
        when(userRepository.existsByEmail("naveen@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass@1234")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(
                new UserResponse(1L, "naveen", "naveen@example.com", "USER", new java.math.BigDecimal("100000.00")));

        UserResponse response = authService.register(request);

        assertThat(response.username()).isEqualTo("naveen");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsException() {
        RegisterRequest request = new RegisterRequest("naveen", "naveen@example.com", "Pass@1234");
        when(userRepository.existsByUsername("naveen")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsTokens() {
        LoginRequest request = new LoginRequest("naveen", "Pass@1234");

        when(userRepository.findByUsername("naveen")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Pass@1234", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(1L, "naveen", "USER")).thenReturn("access-token");
        when(jwtService.generateRawRefreshToken()).thenReturn("raw-refresh-token");
        when(jwtService.getRefreshTokenExpiryMs()).thenReturn(604800000L);
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest("ghost", "Pass@1234");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_revokedToken_revokesAllSessionsAndThrows() {
        RefreshTokenRequest request = new RefreshTokenRequest("stolen-token");
        RefreshToken stored = RefreshToken.builder()
                .id(1L).userId(1L).tokenHash("any").revoked(true)
                .expiresAt(LocalDateTime.now().plusDays(1)).build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("reuse detected");

        verify(refreshTokenRepository).deleteAllByUserId(1L);
    }

    @Test
    void refresh_expiredToken_throwsInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
        RefreshToken stored = RefreshToken.builder()
                .id(1L).userId(1L).tokenHash("any").revoked(false)
                .expiresAt(LocalDateTime.now().minusDays(1)).build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }
}
