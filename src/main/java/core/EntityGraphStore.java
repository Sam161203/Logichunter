package core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent store for EntityNode objects.
 *
 * Changes from original:
 *  CONC-02 FIX: lastCleanup is now volatile.
 *  MEM-06 FIX:  LRU eviction preserves most-recently-seen entities.
 *
 * FIX: getOrCreate() now accepts tenantHint parameter to support Phase 6D
 *      tenant breakout detection. EntityNode.creatorTenantHint is set at
 *      creation time and never changes — this is the only call site.
 *      Backward-compatible overload retained for safety.
 */
public class EntityGraphStore {

    private static final long ENTITY_TTL   = 20 * 60 * 1_000L;
    private static final int  MAX_ENTITIES = 20_000;

    private final Map<String, EntityNode> entities = new ConcurrentHashMap<>();
    private volatile long lastCleanup = 0;

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Primary entry point — called by EntityGraphBuilder with full context
     * including the tenant hint from the current request headers.
     */
    public EntityNode getOrCreate(String type, String id,
                                  String session, String endpoint,
                                  String tenantHint) {
        cleanup();
        String key = type + ":" + id;
        return entities.compute(key, (k, existing) -> {
            if (existing == null)
                // Removed in public version
                return new EntityNode(type, id, "", endpoint, null);
            // Removed in public version
            existing.recordAccess(endpoint, "");
            return existing;
        });
    }

    /** Backward-compatible overload — tenantHint defaults to null. */
    public EntityNode getOrCreate(String type, String id,
                                  String session, String endpoint) {
        return getOrCreate(type, id, session, endpoint, null);
    }

    public Collection<EntityNode> all() { return entities.values(); }

    // ── Cleanup ───────────────────────────────────────────────────────────

    private void cleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < 60_000L) return;
        lastCleanup = now;

        entities.values().removeIf(e -> (now - e.lastSeen) > ENTITY_TTL);

        if (entities.size() > MAX_ENTITIES) {
            List<EntityNode> sorted = new ArrayList<>(entities.values());
            sorted.sort(Comparator.comparingLong(e -> e.lastSeen));
            int toRemove = entities.size() - MAX_ENTITIES;
            for (int i = 0; i < toRemove && i < sorted.size(); i++) {
                entities.remove(sorted.get(i).key());
            }
        }
    }
}