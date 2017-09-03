/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javastack.metrics.jmx;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

/* Original Source:
 * http://svn.apache.org/repos/asf/activemq/branches/activemq-5.6/activemq-core/src/main/java/org/apache/activemq/broker/jmx/AnnotatedMBean.java
 */

/**
 * MBean that looks for method/parameter descriptions in the Info annotation.
 */
public class AnnotatedStandardMBean extends StandardMBean {

	private static final Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();

	static {
		final Class<?>[] p = {
				byte.class, short.class, int.class, long.class, //
				float.class, double.class, //
				char.class, //
				boolean.class,
		};
		for (final Class<?> c : p) {
			primitives.put(c.getName(), c);
		}
	}

	private static ObjectName generateMBeanName(final Class<?> clazz) throws MalformedObjectNameException {
		return new ObjectName(clazz.getPackage().getName() + ":type=" + clazz.getSimpleName());
	}

	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	public static String registerMBean(final Object object) throws JMException {
		final ObjectName objectName = generateMBeanName(object.getClass());
		final MBeanServer context = ManagementFactory.getPlatformMBeanServer();
		final String mbeanName = object.getClass().getName() + "MBean";
		for (final Class c : object.getClass().getInterfaces()) {
			if (mbeanName.equals(c.getName())) {
				context.registerMBean(new AnnotatedStandardMBean(object, c), objectName);
				return objectName.getCanonicalName();
			}
		}
		context.registerMBean(object, objectName);
		return objectName.getCanonicalName();
	}

	public static void unregisterMBean(final Object object) throws JMException {
		final MBeanServer context = ManagementFactory.getPlatformMBeanServer();
		final ObjectName objectName = generateMBeanName(object.getClass());
		context.unregisterMBean(objectName);
	}

	/** Instance where the MBean interface is implemented by another object. */
	public <T> AnnotatedStandardMBean(final T impl, final Class<T> mbeanInterface)
			throws NotCompliantMBeanException {
		super(impl, mbeanInterface);
	}

	/** Instance where the MBean interface is implemented by this object. */
	protected AnnotatedStandardMBean(final Class<?> mbeanInterface) throws NotCompliantMBeanException {
		super(mbeanInterface);
	}

	/** {@inheritDoc} */
	@Override
	protected String getDescription(final MBeanAttributeInfo info) {
		String descr = info.getDescription();
		Method m = getMethod(getMBeanInterface(),
				"get" + info.getName().substring(0, 1).toUpperCase() + info.getName().substring(1));
		if (m == null) {
			m = getMethod(getMBeanInterface(),
					"is" + info.getName().substring(0, 1).toUpperCase() + info.getName().substring(1));
		}
		if (m == null) {
			m = getMethod(getMBeanInterface(),
					"does" + info.getName().substring(0, 1).toUpperCase() + info.getName().substring(1));
		}
		if (m != null) {
			Description d = m.getAnnotation(Description.class);
			if (d != null) {
				descr = d.value();
			}
		}
		return descr;
	}

	/** {@inheritDoc} */
	@Override
	protected String getDescription(final MBeanOperationInfo op) {
		final Method m = getMethod(op);
		String descr = op.getDescription();
		if (m != null) {
			Description d = m.getAnnotation(Description.class);
			if (d != null) {
				descr = d.value();
			}
		}
		return descr;
	}

	/** {@inheritDoc} */
	@Override
	protected String getParameterName(final MBeanOperationInfo op, final MBeanParameterInfo param,
			final int paramNo) {
		final Method m = getMethod(op);
		String name = param.getName();
		if (m != null) {
			for (final Annotation a : m.getParameterAnnotations()[paramNo]) {
				if (Description.class.isInstance(a)) {
					name = Description.class.cast(a).value();
				}
			}
		}
		return name;
	}

	/**
	 * Extracts the Method from the MBeanOperationInfo
	 * 
	 * @param op
	 * @return
	 */
	private Method getMethod(final MBeanOperationInfo op) {
		final MBeanParameterInfo[] params = op.getSignature();
		final String[] paramTypes = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			paramTypes[i] = params[i].getType();
		}
		return getMethod(getMBeanInterface(), op.getName(), paramTypes);
	}

	/**
	 * Returns the Method with the specified name and parameter types for the
	 * given class, null if it doesn't exist.
	 * 
	 * @param mbean
	 * @param method
	 * @param params
	 * @return
	 */
	private static Method getMethod(final Class<?> mbean, final String method, final String... params) {
		try {
			final ClassLoader loader = mbean.getClassLoader();
			final Class<?>[] paramClasses = new Class<?>[params.length];
			for (int i = 0; i < params.length; i++) {
				paramClasses[i] = primitives.get(params[i]);
				if (paramClasses[i] == null) {
					paramClasses[i] = Class.forName(params[i], false, loader);
				}
			}
			return mbean.getMethod(method, paramClasses);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			return null;
		}
	}
}
