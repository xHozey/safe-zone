package com.user.userService.securityFilters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class GatewayIdentityFilterTest {
    private final GatewayIdentityFilter filter = new GatewayIdentityFilter();

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
    void createsAuthenticationFromGatewayHeaders() throws Exception {
        MockHttpServletRequest request = request("/api/v1/users/me");
        request.addHeader("X-User-Id", "user-1");
        request.addHeader("X-User-Role", "ROLE_CLIENT");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("user-1", authentication.getName());
        assertEquals("ROLE_CLIENT", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void leavesRequestsUnauthenticatedWithoutUserAndAllowsBlankRole() throws Exception {
        filter.doFilter(request("/api/v1/users/me"), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest request = request("/api/v1/users/me");
        request.addHeader("X-User-Id", "user-2");
        request.addHeader("X-User-Role", " ");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("user-2", authentication.getName());
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    private static MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }
}
