package com.api.gateway.securityFilters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.api.gateway.utils.JwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

class JwtAuthenticationFilterTest {
    private static final String SECRET = "test-secret-key-for-jwt-filter-coverage";
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new JwtUtil(SECRET));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ignoresTokenWithoutSubjectAndClearsContextForInvalidToken() throws Exception {
        MockHttpServletRequest noSubjectRequest = request("/api/v1/products");
        noSubjectRequest.addHeader("Authorization", "Bearer " + token(null, "CLIENT"));
        filter.doFilter(noSubjectRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("stale", "credentials"));
        MockHttpServletRequest invalidRequest = request("/api/v1/products");
        invalidRequest.addHeader("Authorization", "Bearer invalid");
        filter.doFilter(invalidRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }

    private static String token(String subject, String role) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        var builder = Jwts.builder().claim("role", role);
        if (subject != null) {
            builder.subject(subject);
        }
        return builder.signWith(key).compact();
    }
}
