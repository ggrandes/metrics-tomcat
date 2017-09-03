package org.javastack.metrics;

import org.javastack.metrics.jmx.Description;

public interface MetricsMBean {
	@Description(value = "Count HTTP-1xx")
	int getHttp1xx();

	@Description(value = "Count HTTP-2xx")
	int getHttp2xx();

	@Description(value = "Count HTTP-3xx")
	int getHttp3xx();

	@Description(value = "Count HTTP-4xx")
	int getHttp4xx();

	@Description(value = "Count HTTP-5xx")
	int getHttp5xx();

	@Description(value = "Response Time Last (millis)")
	int getResponseTime();

	@Description(value = "Response Time Average All (millis)")
	int getResponseTimeAvgAll();

	@Description(value = "Response Time Average Last 100 (millis)")
	int getResponseTimeAvg100();

	@Description(value = "Response Time Max Last 100 (millis)")
	int getResponseTimeMax100();
}
