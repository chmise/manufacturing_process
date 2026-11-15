package com.u1mobis.dashboard_backend.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private static JwtUtil jwtUtil(String secret) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", secret);
        ReflectionTestUtils.setField(util, "jwtExpiration", 3600);
        ReflectionTestUtils.setField(util, "refreshExpiration", 7200);
        util.initSigningKey();
        return util;
    }

    @Test
    @DisplayName("JWT_SECRET이 없거나 짧으면 서버가 시작되지 않는다")
    void rejectsMissingOrShortSecret() {
        assertThatThrownBy(() -> jwtUtil("")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> jwtUtil("too-short")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰은 서로의 용도로 쓸 수 없다")
    void accessAndRefreshTokensAreNotInterchangeable() {
        JwtUtil util = jwtUtil("unit-test-secret-key-that-is-long-enough-1234");

        String access = util.generateToken("alice", 1L, 10L);
        String refresh = util.generateRefreshToken("alice");

        assertThat(util.validateAccessToken(access, "alice")).isTrue();
        assertThat(util.validateAccessToken(refresh, "alice")).isFalse();
        assertThat(util.validateRefreshToken(refresh, "alice")).isTrue();
        assertThat(util.validateRefreshToken(access, "alice")).isFalse();
        assertThat(util.getCompanyIdFromToken(access)).isEqualTo(10L);
    }

    @Test
    @DisplayName("다른 키로 서명한 토큰은 거부한다")
    void rejectsTokenSignedWithAnotherKey() {
        JwtUtil ours = jwtUtil("unit-test-secret-key-that-is-long-enough-1234");
        JwtUtil theirs = jwtUtil("another-secret-key-that-is-also-long-enough-99");

        String forged = theirs.generateToken("alice", 1L, 10L);

        assertThat(ours.validateAccessToken(forged, "alice")).isFalse();
    }
}
