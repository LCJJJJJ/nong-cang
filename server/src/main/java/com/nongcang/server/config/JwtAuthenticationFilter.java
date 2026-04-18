package com.nongcang.server.config;

import java.io.IOException;
import java.util.stream.Collectors;

import com.nongcang.server.modules.auth.domain.AuthenticatedUser;
import com.nongcang.server.modules.auth.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenService jwtTokenService;

	private final AuthenticationEntryPoint authenticationEntryPoint;

	public JwtAuthenticationFilter(JwtTokenService jwtTokenService, AuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtTokenService = jwtTokenService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return "/api/auth/login".equals(path) || "/api/auth/refresh".equals(path);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());

		try {
			AuthenticatedUser authenticatedUser = jwtTokenService.parseAccessToken(accessToken);
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					authenticatedUser,
					null,
					authenticatedUser.roles()
							.stream()
							.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
							.collect(Collectors.toList()));
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			filterChain.doFilter(request, response);
		}
		catch (RuntimeException exception) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.commence(request, response,
					new InsufficientAuthenticationException(exception.getMessage(), exception));
		}
	}
}
