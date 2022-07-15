package com.plus1250.jobaTrend.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BEARER_PREFIX = "Bearer";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

//        String jwt = resolveToken(request);
//
//        // 토큰 유효성 검사
//        // 정상 토큰이면 SecurityContext에 저장
//        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
//            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
//            SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.resolveAccessToken(request);
        boolean isAccessTokenValid = accessToken != null && jwtTokenProvider.validateToken(accessToken);

        try {
            if (isAccessTokenValid) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
                if (refreshToken != null && jwtTokenProvider.validateRefreshToken((refreshToken))) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
        }
        chain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}