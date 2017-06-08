package com.octopus.servicecontrol;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

/**
 * A JMX client for the {@code SpringApplicationAdmin} MBean. Permits to obtain
 * information about a given Spring application.
 *
 * @author Stephane Nicoll
 */
public class SpringApplicationAdminClient {
    // Note: see SpringApplicationAdminJmxAutoConfiguration
    static final String DEFAULT_OBJECT_NAME = "org.springframework.boot:type=Admin,name=SpringApplication";

    private final MBeanServerConnection connection;

    private final ObjectName objectName;

    SpringApplicationAdminClient(MBeanServerConnection connection, String jmxName) {
        this.connection = connection;
        this.objectName = toObjectName(jmxName);
    }

    /**
     * Create a connector for an {@link MBeanServer} exposed on the
     * current machine and the current port. Security should be disabled.
     *
     * @param port the port on which the mbean server is exposed
     * @return a connection
     * @throws IOException if the connection to that server failed
     */
    public static JMXConnector connect(int port) throws IOException {
        String url = "service:jmx:rmi:///jndi/rmi://127.0.0.1:" + port + "/jmxrmi";
        JMXServiceURL serviceUrl = new JMXServiceURL(url);
        return JMXConnectorFactory.connect(serviceUrl, null);
    }

    /**
     * Check if the spring application managed by this instance is ready. Returns
     * {@code false} if the mbean is not yet deployed so this method should be repeatedly
     * called until a timeout is reached.
     *
     * @return {@code true} if the application is ready to service requests
     * @throws JmxException if the JMX service could not be contacted
     */
    public boolean isReady() {
        try {
            return (Boolean) this.connection.getAttribute(this.objectName, "Ready");
        } catch (InstanceNotFoundException ex) {
            return false; // Instance not available yet
        } catch (AttributeNotFoundException ex) {
            throw new IllegalStateException(
                    "Unexpected: attribute 'Ready' not available", ex);
        } catch (ReflectionException ex) {
            throw new JmxException("Failed to retrieve Ready attribute",
                    ex.getCause());
        } catch (MBeanException ex) {
            throw new JmxException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new JmxException(ex.getMessage(), ex);
        }
    }

    /**
     * Stop the application managed by this instance.
     *
     * @throws JmxException              if the JMX service could not be contacted
     * @throws IOException               if an I/O error occurs
     * @throws InstanceNotFoundException if the lifecycle mbean cannot be found
     */
    public void stop() throws IOException,
            InstanceNotFoundException {
        try {
            this.connection.invoke(this.objectName, "shutdown", null, null);
        } catch (ReflectionException ex) {
            throw new JmxException("Shutdown failed", ex.getCause());
        } catch (MBeanException ex) {
            throw new JmxException("Could not invoke shutdown operation", ex);
        }
    }

    private ObjectName toObjectName(String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException("Invalid jmx name '" + name + "'");
        }
    }
}
