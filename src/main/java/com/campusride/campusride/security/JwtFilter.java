package com.campusride.campusride.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.campusride.campusride.model.User;
import com.campusride.campusride.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Get Authorization header from request
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        // Step 2: Check if header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            token = authHeader.substring(7);
            // Extract email from token
            email = jwtUtil.extractEmail(token);
        }

        // Step 3: If email found and no authentication set yet
        if (email != null &&
            SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            // Find user in database
            User user = userRepository
                .findByEmail(email)
                .orElse(null);

            // Step 4: Validate token
            if (user != null &&
                jwtUtil.validateToken(token, email)) {

                // Step 5: Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name()))
                    );

                authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                        .buildDetails(request)
                );

                // Step 6: Set authentication in security context
                SecurityContextHolder.getContext()
                    .setAuthentication(authToken);
            }
        }

        // Step 7: Continue to next filter/controller
        filterChain.doFilter(request, response);
    }
}