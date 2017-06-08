package com.octopus.servicecontrol;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stop a Spring Boot application as a service.
 *
 * @author Matthew Casperson
 */
public class StopSpringBootService {
    private static final Logger LOGGER = Logger.getLogger(StopSpringBootService.class.getName());

    public static void main(String[] args) throws Exception {
        System.out.println("Stopping Spring Boot application...");
        final int jmxPort = Integer.parseInt(args[0]);
        final String jmxName = SpringApplicationAdminClient.DEFAULT_OBJECT_NAME;
        final JMXConnector connector = SpringApplicationAdminClient.connect(jmxPort);
        try {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            try {
                new SpringApplicationAdminClient(connection, jmxName).stop();
            } catch (InstanceNotFoundException ex) {
                throw new IllegalStateException("Spring application lifecycle JMX bean not " +
                        "found, could not stop application gracefully", ex);
            }
        } finally {
            try {
                connector.close();
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, "Exception thrown during connector close: " + ex.toString());
            }
        }
    }
}
