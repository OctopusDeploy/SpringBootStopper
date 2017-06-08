package com.octopus.servicecontrol;

/**
 * Represents an exception contacting the Spring Boot
 * application via JMX
 */
public class JmxException extends RuntimeException {
    public JmxException() {
        super();
    }

    public JmxException(String message) {
        super(message);
    }

    public JmxException(String message, Throwable cause) {
        super(message, cause);
    }

    public JmxException(Throwable cause) {
        super(cause);
    }
}
