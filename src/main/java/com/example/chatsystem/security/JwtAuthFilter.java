package com.example.chatsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;

    @Autowired
    public JwtAuthFilter(JwtService jwtService, MyUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    Cookie[] cookies = request.getCookies();
    String token;
    //String username = null;
    String userId = null;

    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("jwt")) {
                try {
                    token = cookie.getValue();
                    //username = jwtService.extractUsername(token);
                    userId = jwtService.extractUserId(token);
                }catch (Exception e){
                    Cookie emptyCookie = new Cookie(cookie.getName(), "");
                    emptyCookie.setPath("/");
                    emptyCookie.setMaxAge(0);
                    emptyCookie.setDomain(request.getServerName());
                    emptyCookie.setSecure(true);
                    emptyCookie.setHttpOnly(true);
                    response.addCookie(emptyCookie);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }
    }

    if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null){
        try {
            UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }catch (UsernameNotFoundException e){
            Cookie emptyCookie = new Cookie("jwt", "");
            emptyCookie.setPath("/");
            emptyCookie.setMaxAge(0);
            emptyCookie.setDomain(request.getServerName());
            emptyCookie.setSecure(true);
            emptyCookie.setHttpOnly(true);
            response.addCookie(emptyCookie);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }

    filterChain.doFilter(request, response);
}
}