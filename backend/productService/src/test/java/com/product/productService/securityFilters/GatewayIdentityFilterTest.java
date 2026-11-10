package com.product.productService.securityFilters;

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
    void preservesPrefixedRoleAndLeavesBlankOrMissingIdentityWithoutAuthorities() throws Exception {
        MockHttpServletRequest prefixedRequest = request("/api/v1/products/mine");
        prefixedRequest.addHeader("X-User-Id", "seller-1");
        prefixedRequest.addHeader("X-User-Role", "ROLE_SELLER");
        filter.doFilter(prefixedRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertEquals("ROLE_SELLER",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());

        SecurityContextHolder.clearContext();
        filter.doFilter(request("/api/v1/products/mine"), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest blankRoleRequest = request("/api/v1/products/mine");
        blankRoleRequest.addHeader("X-User-Id", "user-2");
        blankRoleRequest.addHeader("X-User-Role", " ");
        filter.doFilter(blankRoleRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    private static MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }
}
