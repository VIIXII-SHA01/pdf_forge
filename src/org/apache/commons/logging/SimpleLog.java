package org.apache.commons.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleLog implements Log {

    private static final Map<String, SimpleLog> CACHE = new ConcurrentHashMap<>();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String name;

    private SimpleLog(String name) {
        this.name = name;
    }

    public static Log getLog(String name) {
        return CACHE.computeIfAbsent(name, SimpleLog::new);
    }

    private void print(String level, Object message, Throwable t) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String msg = message == null ? "null" : message.toString();
        System.err.print("[" + timestamp + "] [" + level + "] " + name + " - " + msg);
        if (t != null) {
            System.err.print("\n");
            t.printStackTrace(System.err);
        } else {
            System.err.print("\n");
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isFatalEnabled() {
        return true;
    }

    @Override
    public void trace(Object message) {
        print("TRACE", message, null);
    }

    @Override
    public void trace(Object message, Throwable t) {
        print("TRACE", message, t);
    }

    @Override
    public void debug(Object message) {
        print("DEBUG", message, null);
    }

    @Override
    public void debug(Object message, Throwable t) {
        print("DEBUG", message, t);
    }

    @Override
    public void info(Object message) {
        print("INFO", message, null);
    }

    @Override
    public void info(Object message, Throwable t) {
        print("INFO", message, t);
    }

    @Override
    public void warn(Object message) {
        print("WARN", message, null);
    }

    @Override
    public void warn(Object message, Throwable t) {
        print("WARN", message, t);
    }

    @Override
    public void error(Object message) {
        print("ERROR", message, null);
    }

    @Override
    public void error(Object message, Throwable t) {
        print("ERROR", message, t);
    }

    @Override
    public void fatal(Object message) {
        print("FATAL", message, null);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        print("FATAL", message, t);
    }
}
