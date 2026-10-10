package br.com.fiap.wefood.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * A malformed/garbage Bearer token used to let JwtUtil's
     * JwtException/IllegalArgumentException escape doFilterInternal
     * uncaught, which Spring turns into a 500 Internal Server Error rather
     * than the intended 401 Unauthorized. The filter must swallow the
     * parsing failure, leave the request unauthenticated, and let the
     * filter chain continue so Spring Security's own entry point produces
     * the correct 401/403 downstream.
     */
    @Test
    void doFilterInternal_malformedBearerToken_doesNotThrowAndLeavesRequestUnauthenticated() throws Exception {
        JwtUtil jwtUtil = new JwtUtil();
        setJwtSecret(jwtUtil, "a-test-secret-that-is-long-enough-for-hs256-signing");
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        org.mockito.Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer garbage.not.a.jwt");

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_authorizationHeaderWithoutBearerPrefix_doesNotThrow() throws Exception {
        JwtUtil jwtUtil = new JwtUtil();
        setJwtSecret(jwtUtil, "a-test-secret-that-is-long-enough-for-hs256-signing");
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        org.mockito.Mockito.when(request.getHeader("Authorization")).thenReturn("abc");

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));

        verify(filterChain).doFilter(request, response);
    }

    private static void setJwtSecret(JwtUtil jwtUtil, String secret) throws Exception {
        var field = JwtUtil.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(jwtUtil, secret);
    }
}
