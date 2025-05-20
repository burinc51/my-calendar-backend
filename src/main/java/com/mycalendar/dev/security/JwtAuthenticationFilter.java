package com.mycalendar.dev.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Refresh-Token");
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = getAccessTokenFromRequest(request);
        String refreshToken = getRefreshTokenFromRequest(request);
        if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
            String username = jwtTokenProvider.getUsername(accessToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        if (accessToken != null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Access Token");
            if (jwtTokenProvider.isTokenRevoked(accessToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Token has been revoked");
                return;
            }
            return;
        }

        if (refreshToken != null) {
            if (jwtTokenProvider.isRefreshTokenRevoked(refreshToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh Token has been revoked");
                return;
            }

            if (!jwtTokenProvider.validateToken(refreshToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
