package com.nongcang.server.common.trace;

import org.slf4j.MDC;

public final class TraceIdContext {

	public static final String TRACE_ID_KEY = "traceId";

	public static final String TRACE_ID_HEADER = "X-Trace-Id";

	private TraceIdContext() {
	}

	public static String getTraceId() {
		return MDC.get(TRACE_ID_KEY);
	}

	public static void setTraceId(String traceId) {
		MDC.put(TRACE_ID_KEY, traceId);
	}

	public static void clear() {
		MDC.remove(TRACE_ID_KEY);
	}
}
