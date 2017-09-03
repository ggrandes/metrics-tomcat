# metrics-tomcat

Metrics for Tomcat via JMX, usable with jmxtrans-agent and Graphite/Grafana. Open Source Java project under Apache License v2.0

### Current Stable Version is [1.0.0](https://search.maven.org/#search|ga|1|g%3Aorg.javastack%20a%3Ametrics-tomcat)

---

## DOC

#### Exported Metrics

| Name | Description |
| :--- | :--- |
| Http1xx | Count HTTP-1xx |
| Http2xx | Count HTTP-2xx |
| Http3xx | Count HTTP-3xx |
| Http4xx | Count HTTP-4xx |
| Http5xx | Count HTTP-5xx |
| ResponseTime | Response Time Last (millis) |
| ResponseTimeAvgAll | Response Time Average All (millis) |
| ResponseTimeAvg100 | Response Time Average Last 100 (millis) |
| ResponseTimeMax100 | Response Time Max Last 100 (millis) |

#### Usage

  1. Put `metrics-tomcat-x.x.x.jar` into `${CATALINA_HOME}/lib/`
  2. Configure Metrics valve in `${CATALINA_BASE}/server.xml` inside Engine or Host section.

```xml
<?xml version='1.0' encoding='utf-8'?>
<Server port="8005" shutdown="SHUTDOWN">
  ...
  <Service name="Catalina">
    ...
    <Engine name="Catalina" defaultHost="localhost">
      <Valve className="org.javastack.metrics.MetricsValve" />
```

  3. If you use Jmxtrans-Agent, configure `jmxtrans-agent.xml`, example:

```xml
<query objectName="org.javastack.metrics:type=Metrics" 
       attributes="Http1xx,Http2xx,Http3xx,Http4xx,Http5xx,ResponseTime,ResponseTimeAvg100,ResponseTimeMax100"
       resultAlias="metrics.tomcat.#attribute#" />
```

---

## MAVEN

    <dependency>
        <groupId>org.javastack</groupId>
        <artifactId>metrics-tomcat</artifactId>
        <version>1.0.0</version>
    </dependency>

---
Inspired in [Tomcat](https://tomcat.apache.org/) and [jmxtrans-agent](https://github.com/jmxtrans/jmxtrans-agent), this code is Java-minimalistic version.
