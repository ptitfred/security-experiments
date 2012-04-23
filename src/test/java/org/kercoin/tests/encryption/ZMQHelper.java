package org.kercoin.tests.encryption;

public final class ZMQHelper {

    private ZMQHelper() {}

    /**
     * Build a convenient inproc url based on an object identity (class name + instance hashcode) and a topic.
     * @param object
     * @param topic
     * @return
     */
    public static String getInprocUrl(Object object, String topic) {
        return String.format("inproc://%s@%s/%s", object.getClass().getName(), object.hashCode(), topic);
    }

}
