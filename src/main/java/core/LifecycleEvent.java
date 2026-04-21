package core;

public class LifecycleEvent {
    public final String endpoint;
    public final String session;
    public final long timestamp;

    public LifecycleEvent(String endpoint, String session) {
        this.endpoint = endpoint;
        this.session = session;
        this.timestamp = System.currentTimeMillis();
    }
}