package org.javastack.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;

import org.javastack.metrics.jmx.AnnotatedStandardMBean;

public class Metrics implements MetricsMBean {
	private static final long MAX_L = (Long.MAX_VALUE >>> 1);
	private static final int MAX_I = (Integer.MAX_VALUE >>> 1);
	private static Metrics INSTANCE = null;

	private final AtomicInteger http1xx = new AtomicInteger();
	private final AtomicInteger http2xx = new AtomicInteger();
	private final AtomicInteger http3xx = new AtomicInteger();
	private final AtomicInteger http4xx = new AtomicInteger();
	private final AtomicInteger http5xx = new AtomicInteger();
	private final AtomicInteger resTime = new AtomicInteger();
	private final AtomicLong resTimeAcc = new AtomicLong();
	private final AtomicIntegerArray resTime100 = new AtomicIntegerArray(100);
	private final AtomicInteger resCount = new AtomicInteger();

	private String name = null;

	public static Metrics getInstance() throws JMException {
		if (INSTANCE == null) {
			synchronized (Metrics.class) {
				if (INSTANCE == null) {
					final Metrics m = new Metrics();
					m.setName(AnnotatedStandardMBean.registerMBean(m));
					INSTANCE = m;
				}
			}
		}
		return INSTANCE;
	}

	public static void destroy() throws JMException {
		synchronized (Metrics.class) {
			if (INSTANCE != null) {
				AnnotatedStandardMBean.unregisterMBean(INSTANCE);
				INSTANCE = null;
			}
		}
	}

	private Metrics() {
	}

	private void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void incHttp(final int code) {
		final int c = code / 100;
		if (c == 1) {
			incHttp1xx();
		} else if (c == 2) {
			incHttp2xx();
		} else if (c == 3) {
			incHttp3xx();
		} else if (c == 4) {
			incHttp4xx();
		} else if (c == 5) {
			incHttp5xx();
		}
	}

	public void incHttp1xx() {
		http1xx.incrementAndGet();
	}

	public void incHttp2xx() {
		http2xx.incrementAndGet();
	}

	public void incHttp3xx() {
		http3xx.incrementAndGet();
	}

	public void incHttp4xx() {
		http4xx.incrementAndGet();
	}

	public void incHttp5xx() {
		http5xx.incrementAndGet();
	}

	public void addResponseTime(final int millis) {
		resTime.set(millis);
		// Accumulated Total
		final int c = resCount.getAndIncrement();
		final long a = resTimeAcc.getAndAdd(millis);
		// Last 100 responses
		resTime100.set(c % resTime100.length(), millis);
		if ((a >= MAX_L) || (c >= MAX_I)) {
			// Reset
			resTimeAcc.set(resTimeAcc.get() / resCount.getAndSet(1));
		}
	}

	// MBean

	@Override
	public int getHttp1xx() {
		return http1xx.get();
	}

	@Override
	public int getHttp2xx() {
		return http2xx.get();
	}

	@Override
	public int getHttp3xx() {
		return http3xx.get();
	}

	@Override
	public int getHttp4xx() {
		return http4xx.get();
	}

	@Override
	public int getHttp5xx() {
		return http5xx.get();
	}

	@Override
	public int getResponseTime() {
		return resTime.get();
	}

	@Override
	public int getResponseTimeAvgAll() {
		final int c = resCount.get();
		if (c < 1) {
			return 0;
		}
		return (int) (resTimeAcc.get() / c);
	}

	@Override
	public int getResponseTimeAvg100() {
		final int c = resCount.get();
		if (c < 1) {
			return 0;
		}
		final int len = Math.min(c, resTime100.length());
		long a = 0;
		for (int i = 0; i < len; i++) {
			a += resTime100.get(i);
		}
		return (int) (a / len);
	}

	@Override
	public int getResponseTimeMax100() {
		final int c = resCount.get();
		if (c < 1) {
			return 0;
		}
		final int len = Math.min(c, resTime100.length());
		int max = 0;
		for (int i = 0; i < len; i++) {
			final int a = resTime100.get(i);
			if (a > max) {
				max = a;
			}
		}
		return max;
	}
}