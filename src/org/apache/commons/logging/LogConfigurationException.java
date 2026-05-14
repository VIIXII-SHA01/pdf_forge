package org.apache.commons.logging;

public class LogConfigurationException extends RuntimeException {

    public LogConfigurationException() {
        super();
    }

    public LogConfigurationException(String message) {
        super(message);
    }

    public LogConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogConfigurationException(Throwable cause) {
        super(cause);
    }
}
