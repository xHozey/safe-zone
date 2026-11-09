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
    void skipsAuthenticationRoutes() throws Exception {
        MockHttpServletRequest request = request("/api/v1/auth/login");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(request, chain.getRequest());
    }

    @Test
    void passesRequestsWithoutBearerTokenThroughUnchanged() throws Exception {
        MockHttpServletRequest request = request("/api/v1/products");
        request.addHeader("Authorization", "Basic credentials");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(request, chain.getRequest());
    }

    @Test
    void authenticatesBearerTokenAndSuppliesIdentityHeaders() throws Exception {
        MockHttpServletRequest request = request("/api/v1/products");
        request.addHeader("Authorization", "Bearer " + token("user-1", "SELLER"));
        request.addHeader("X-Existing", "existing");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("user-1", authentication.getName());
        assertEquals("ROLE_SELLER", authentication.getAuthorities().iterator().next().getAuthority());

        HttpServletRequest wrapped = (HttpServletRequest) chain.getRequest();
        assertEquals("user-1", wrapped.getHeader("x-user-id"));
        assertEquals("ROLE_SELLER", wrapped.getHeader("X-User-Role"));
        assertEquals("existing", wrapped.getHeader("X-Existing"));
        assertEquals(Collections.list(wrapped.getHeaders("X-User-Id")), java.util.List.of("user-1"));
        assertEquals(Collections.list(wrapped.getHeaders("X-Existing")), java.util.List.of("existing"));
        assertTrue(Collections.list(wrapped.getHeaderNames()).contains("X-User-Id"));
        assertTrue(Collections.list(wrapped.getHeaderNames()).contains("X-User-Role"));
    }

    @Test
    void acceptsPrefixedRoleAndLeavesBlankRoleWithoutAuthority() throws Exception {
        MockHttpServletRequest prefixedRequest = request("/api/v1/products");
        prefixedRequest.addHeader("Authorization", "Bearer " + token("seller-1", "ROLE_SELLER"));
        MockFilterChain prefixedChain = new MockFilterChain();
        filter.doFilter(prefixedRequest, new MockHttpServletResponse(), prefixedChain);
        assertEquals("ROLE_SELLER",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());

        SecurityContextHolder.clearContext();
        MockHttpServletRequest blankRoleRequest = request("/api/v1/products");
        blankRoleRequest.addHeader("Authorization", "Bearer " + token("user-2", " "));
        MockFilterChain blankRoleChain = new MockFilterChain();
        filter.doFilter(blankRoleRequest, new MockHttpServletResponse(), blankRoleChain);
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());

        HttpServletRequest wrapped = (HttpServletRequest) blankRoleChain.getRequest();
        assertNull(wrapped.getHeader("X-User-Role"));
        assertFalse(Collections.list(wrapped.getHeaderNames()).contains("X-User-Role"));
        assertTrue(Collections.list(wrapped.getHeaders("X-User-Role")).isEmpty());
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
