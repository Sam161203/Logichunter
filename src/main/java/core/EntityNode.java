package core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single tracked entity (e.g., Order, User, Invoice).
 *
 * Changes from original:
 *  CONC-01 FIX: blindGuessChecked and crossSessionAlerted are now volatile.
 *  MEM-05 FIX:  lifecycle list capped at 20 events (sliding window).
 *
 * FIX: Added creatorTenantHint and tenantBreakoutAlerted to complete
 *      Phase 6D SaaS tenant breakout detection. Previously detectTenantBreakout()
 *      in EntityGraphBuilder was empty because EntityNode had no way to store
 *      the tenant that created the entity. These two fields close that gap.
 *
 *      creatorTenantHint — the tenant header value (e.g. X-Tenant-ID) observed
 *        at entity creation time. Null if no tenant header was present.
 *      tenantBreakoutAlerted — prevents duplicate alerts for the same entity.
 *
 *      Backward-compatible: old 4-arg constructor delegates to new 5-arg constructor
 *      with tenantHint=null. No other call site changes.
 */
public class EntityNode {

    public final String entityType;
    public final String entityId;
    public final String creatorSession;
    public final String creationEndpoint;
    public final long   createdAt;
    public volatile long lastSeen = System.currentTimeMillis();

    // Phase 6D: tenant hint recorded at creation time (may be null)
    public final String creatorTenantHint;

    // CONC-01 FIX: volatile ensures cross-thread visibility
    public volatile boolean blindGuessChecked    = false;
    public volatile boolean crossSessionAlerted  = false;
    public volatile boolean tenantBreakoutAlerted = false; // Phase 6D

    public final Set<String>           accessingSessions = ConcurrentHashMap.newKeySet();
    private final List<LifecycleEvent> lifecycle         = new ArrayList<>();

    private static final int MAX_LIFECYCLE = 20;

    // ── Full constructor (used by EntityGraphStore) ───────────────────────
    public EntityNode(String type, String id, String session,
                      String endpoint, String tenantHint) {
        this.entityType         = type;
        this.entityId           = id;
        // Removed in public version
        this.creatorSession     = "";
        this.creationEndpoint   = endpoint;
        this.createdAt          = System.currentTimeMillis();
        // Removed in public version
        this.creatorTenantHint  = null;

        // Removed in public version
    }

    // ── Backward-compatible 4-arg constructor ─────────────────────────────
    public EntityNode(String type, String id, String session, String endpoint) {
        this(type, id, session, endpoint, null);
    }

    public void recordAccess(String endpoint, String session) {
        // Removed in public version
        lastSeen = System.currentTimeMillis();
    }

    public List<LifecycleEvent> getLifecycleCopy() {
        synchronized (this) { return new ArrayList<>(lifecycle); }
    }

    public String key() { return entityType + ":" + entityId; }
}