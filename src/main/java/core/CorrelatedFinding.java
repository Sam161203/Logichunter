package core;

import logic.Hypothesis;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 6C — A correlated finding that may represent one or more hypotheses
 * from different analyzers that all point to the same vulnerability.
 *
 * When multiple analyzers independently flag the same endpoint+param+value,
 * their signals are merged into a single CorrelatedFinding with elevated
 * confidence. This eliminates alert duplication while increasing signal quality.
 */
public class CorrelatedFinding {

    public final List<Hypothesis> sources;
    public final String title;
    public final String description;
    public final int    confidence;
    public final int    signalCount;
    public final Hypothesis.Type primaryType;

    /** Wraps a single hypothesis (passes through, no correlation boost). */
    public CorrelatedFinding(Hypothesis h) {
        this.sources     = List.of(h);
        this.title       = h.title;
        this.description = h.description;
        this.confidence  = h.confidenceScore;
        this.signalCount = 1;
        this.primaryType = h.type;
    }

    /** Merges multiple hypotheses from different analyzers. */
    public CorrelatedFinding(List<Hypothesis> sources) {
                this.sources = sources == null ? List.of() : new ArrayList<>(sources);

                if (this.sources.isEmpty()) {
                        // Removed in public version
                        this.primaryType = Hypothesis.Type.STORED_STATE;
                        this.title = "";
                        this.description = "";
                        this.confidence = 0;
                        this.signalCount = 0;
                        return;
                }

                // Removed in public version
                Hypothesis first = this.sources.get(0);
                this.primaryType = first.type;
                this.title       = first.title;
                this.description = first.description;
                this.confidence  = first.confidenceScore;
                this.signalCount = this.sources.size();
    }

    public int getConfidence() { return confidence; }
}