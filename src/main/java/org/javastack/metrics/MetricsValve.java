package org.javastack.metrics;

import java.io.IOException;

import javax.management.JMException;
import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class MetricsValve extends ValveBase {
    private static final Log log = LogFactory.getLog(MetricsValve.class);

	private Metrics metrics;

	public MetricsValve() {
		super(true);
	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		try {
			metrics = Metrics.getInstance();
		} catch (JMException e) {
			throw new LifecycleException(e);
		}
		log.info("Registered JMX: " + metrics.getName());
		setState(LifecycleState.STARTING);
	}

	@Override
	protected synchronized void stopInternal() throws LifecycleException {
		final String name = metrics.getName();
		setState(LifecycleState.STOPPING);
		try {
			Metrics.destroy();
			metrics = null;
		} catch (JMException e) {
			throw new LifecycleException(e);
		}
		log.info("Unregistered JMX: " + name);
	}

	@Override
	public void invoke(final Request request, final Response response) throws IOException, ServletException {
		final long ts = System.currentTimeMillis();
		try {
			getNext().invoke(request, response);
		} finally {
			metrics.addResponseTime((int) (System.currentTimeMillis() - ts));
			metrics.incHttp(response.getStatus());
		}
	}
}
