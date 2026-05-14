package org.apache.commons.logging;

public abstract class LogFactory {

    private static final SimpleLogFactory FACTORY = new SimpleLogFactory();

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz == null ? "null" : clazz.getName());
    }

    public static Log getLog(String name) {
        return FACTORY.getInstance(name);
    }

    public static LogFactory getFactory() {
        return FACTORY;
    }

    public static void release(Class<?> clazz) {
        // no-op
    }

    public static void release(String name) {
        // no-op
    }

    public static void releaseAll() {
        // no-op
    }

    public static void setAttribute(String name, Object value) {
        // no-op
    }

    public static Object getAttribute(String name) {
        return null;
    }

    public static String[] getAttributeNames() {
        return new String[0];
    }

    protected abstract Log getInstance(String name);

    private static final class SimpleLogFactory extends LogFactory {
        @Override
        protected Log getInstance(String name) {
            return SimpleLog.getLog(name);
        }
    }
}
