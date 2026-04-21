package core;

import java.util.Objects;

/**
 * Represents a single normalized observation event from proxy traffic.
 *
 * Changes from original:
 *  - Added host field for per-target namespace isolation (Phase 6A)
 *  - Added tenantHint field for SaaS tenant breakout detection (Phase 6D)
 *  - Original 6-arg constructor kept for full backward compatibility
 */
public class StateNode {

    public enum EventType {
        REQUEST,
        RESPONSE
    }

    // ── Core event fields ─────────────────────────────────────────────────
    public final String  endpoint;
    public final String  param;
    public final String  injectedValue;
    public final String  sessionContext;
    public final String  httpMethod;
    public final Integer statusCode;
    public final long    timestamp;
    public final EventType eventType;

    // ── Phase 6A: host isolation ──────────────────────────────────────────
    /** Hostname (e.g. "api.example.com"). Used to namespace all map keys. */
    public final String host;

    // ── Phase 6D: tenant detection ────────────────────────────────────────
    /**
     * Value extracted from a tenant-identifying header or body field.
     * Examples: X-Tenant-ID, X-Org-ID, org_id in JSON body.
     * Null if no tenant hint was found.
     */
    public final String tenantHint;

    // ── Full constructor ──────────────────────────────────────────────────
    public StateNode(String  endpoint,
                     String  param,
                     String  injectedValue,
                     String  sessionContext,
                     String  httpMethod,
                     Integer statusCode,
                     String  host,
                     String  tenantHint) {

        this.endpoint       = safe(endpoint);
        this.param          = safe(param);
        this.injectedValue  = safe(injectedValue);
        this.sessionContext = safe(sessionContext);
        this.httpMethod     = normalizeMethod(httpMethod);
        this.statusCode     = statusCode;
        this.timestamp      = System.currentTimeMillis();
        this.host           = safe(host);
        this.tenantHint     = tenantHint; // may be null — that is intentional

        this.eventType = (statusCode == null) ? EventType.REQUEST : EventType.RESPONSE;
    }

    // ── Backward-compatible constructor (original callers need no change) ─
    public StateNode(String  endpoint,
                     String  param,
                     String  injectedValue,
                     String  sessionContext,
                     String  httpMethod,
                     Integer statusCode) {
        this(endpoint, param, injectedValue, sessionContext, httpMethod, statusCode, "", null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String safe(String v)             { return v == null ? "" : v; }
    private String normalizeMethod(String m)  { return m == null ? "UNKNOWN" : m.toUpperCase(); }

    // ── Semantic helpers ──────────────────────────────────────────────────
    public boolean isReadEvent()  { return eventType == EventType.RESPONSE; }

    public boolean isWriteEvent() {
        return "POST".equals(httpMethod) || "PUT".equals(httpMethod)
            || "PATCH".equals(httpMethod) || "DELETE".equals(httpMethod);
    }

    public boolean isAuthenticated() {
        // Removed in public version
        return false;
    }

    public boolean isSameSession(StateNode other) {
        // Removed in public version
        return false;
    }

    public boolean sameEndpoint(StateNode other) {
        return other != null && Objects.equals(this.endpoint, other.endpoint);
    }

    public boolean sameParameter(StateNode other) {
        return other != null && Objects.equals(this.param, other.param);
    }

    /**
     * Namespace-qualified key for map storage.
     * Includes host so entries from different targets never collide.
     */
    public String namespacedEndpoint() {
        return host.isEmpty() ? endpoint : host + "|" + endpoint;
    }

    @Override
    public String toString() {
        return "StateNode{endpoint='" + endpoint + '\''
                + ", param='" + param + '\''
                + ", value='" + injectedValue + '\''
                + ", session='<redacted>'"
                + ", method='" + httpMethod + '\''
                + ", status=" + statusCode
                + ", type=" + eventType
                + ", host='" + host + '\''
                + ", tenant='<redacted>'"
                + ", time=" + timestamp + '}';
    }
}