package com.ppquang06.uniclassapp;

import static org.junit.Assert.*;

import com.ppquang06.uniclassapp.utils.AuthUiHelper;

import org.junit.Test;

public class AuthUiHelperTest {

    @Test
    public void testPasswordValidationRules() {
        assertFalse(AuthUiHelper.hasMinLength("Aa1!"));
        assertTrue(AuthUiHelper.hasMinLength("Aa1!5678"));

        assertFalse(AuthUiHelper.hasUppercase("uniclass2026!"));
        assertTrue(AuthUiHelper.hasUppercase("Uniclass2026!"));

        assertFalse(AuthUiHelper.hasLowercase("UNICLASS2026!"));
        assertTrue(AuthUiHelper.hasLowercase("UniClass2026!"));

        assertFalse(AuthUiHelper.hasDigit("UniClass!"));
        assertTrue(AuthUiHelper.hasDigit("UniClass2026!"));

        assertFalse(AuthUiHelper.hasSpecialChar("UniClass2026"));
        assertTrue(AuthUiHelper.hasSpecialChar("UniClass2026@"));

        assertTrue(AuthUiHelper.isPasswordValid("UniClass2026@"));
        assertFalse(AuthUiHelper.isPasswordValid("uniclass2026@")); // missing uppercase
        assertFalse(AuthUiHelper.isPasswordValid("UniClass@")); // length < 8
    }

    @Test
    public void testPasswordMatch() {
        assertTrue(AuthUiHelper.doPasswordsMatch("Secret123!", "Secret123!"));
        assertFalse(AuthUiHelper.doPasswordsMatch("Secret123!", "Secret123"));
        assertFalse(AuthUiHelper.doPasswordsMatch(null, "Secret123!"));
    }

    @Test
    public void testExtractSixDigits() {
        assertEquals("849201", AuthUiHelper.extractSixDigits("849201"));
        assertEquals("849201", AuthUiHelper.extractSixDigits("Mã xác thực của bạn là: 849201."));
        assertEquals("123456", AuthUiHelper.extractSixDigits("[123456] UniClass OTP"));
        assertNull(AuthUiHelper.extractSixDigits("12345"));
        assertNull(AuthUiHelper.extractSixDigits(null));
    }

    @Test
    public void testOtpCooldownReuse() {
        long now = 100_000L;
        long sentAt = 70_000L; // 30s ago
        long cooldown = 60_000L;

        // Same email within 60s -> should reuse
        assertTrue(AuthUiHelper.shouldReuseActiveOtpCooldown("user@uit.edu.vn", "user@uit.edu.vn", now, sentAt, cooldown));
        assertEquals(30, AuthUiHelper.getRemainingCooldownSeconds(now, sentAt, cooldown));

        // Different email -> do not reuse
        assertFalse(AuthUiHelper.shouldReuseActiveOtpCooldown("new@uit.edu.vn", "user@uit.edu.vn", now, sentAt, cooldown));

        // Expired -> do not reuse
        long expiredSentAt = 30_000L; // 70s ago
        assertFalse(AuthUiHelper.shouldReuseActiveOtpCooldown("user@uit.edu.vn", "user@uit.edu.vn", now, expiredSentAt, cooldown));
        assertEquals(0, AuthUiHelper.getRemainingCooldownSeconds(now, expiredSentAt, cooldown));
    }
}
