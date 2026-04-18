package com.nongcang.server.common.trace;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestTraceId = request.getHeader(TraceIdContext.TRACE_ID_HEADER);
		String traceId = StringUtils.hasText(requestTraceId) ? requestTraceId : UUID.randomUUID().toString().replace("-", "");

		TraceIdContext.setTraceId(traceId);
		request.setAttribute(TraceIdContext.TRACE_ID_KEY, traceId);
		response.setHeader(TraceIdContext.TRACE_ID_HEADER, traceId);

		try {
			filterChain.doFilter(request, response);
		}
		finally {
			TraceIdContext.clear();
		}
	}
}
