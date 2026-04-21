package core;

import java.util.concurrent.*;

/**
 * Asynchronous fan-out dispatcher for StateNode events.
 *
 * CRITICAL FIX (Priority 1):
 *  Original: every analyzer ran synchronously on the Burp proxy thread.
 *  Fixed:    submit() is non-blocking (<1 µs). Analyzers run on a single
 *            dedicated daemon thread named "LogicHunter-Analyzer".
 *            If the queue is full under extreme load, the event is silently
 *            dropped — this is correct: dropping one observation is
 *            infinitely better than stalling the user's browser traffic.
 *
 * Also supports submitTask(Runnable) so ObservationStore can offload its
 * heavier response-body work to the same background thread.
 */
public class StateModelDispatcher {

    private static final int QUEUE_CAPACITY = 500;

    private final CopyOnWriteArrayList<StateAnalyzer> analyzers = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final ExecutorService executor;

    public StateModelDispatcher() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "LogicHunter-Analyzer");
            t.setDaemon(true);                        // dies with Burp JVM, no cleanup needed
            t.setPriority(Thread.NORM_PRIORITY - 1);  // below proxy threads
            return t;
        });
        executor.submit(this::drainLoop);
    }

    // ── Registration ──────────────────────────────────────────────────────

    public void registerAnalyzer(StateAnalyzer analyzer) {
        if (analyzer != null) analyzers.add(analyzer);
    }

    // ── Non-blocking submit (called from proxy thread) ────────────────────

    /**
     * Wraps the StateNode in a dispatch Runnable and offers it to the queue.
     * Returns immediately regardless of queue state.
     */
    public void submit(StateNode node) {
        if (node == null) return;
        queue.offer(() -> dispatch(node));   // offer = non-blocking, drops if full
    }

    /**
     * Kept for backward compatibility with any callers still using old name.
     */
    public void processObservation(StateNode node) {
        submit(node);
    }

    /**
     * Submit an arbitrary Runnable to the analyzer thread.
     * Used by ObservationStore to offload heavy response-body processing.
     */
    public void submitTask(Runnable task) {
        if (task == null) return;
        queue.offer(task);
    }

    // ── Drain loop (runs on analyzer thread forever) ──────────────────────

    private void drainLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Runnable task = queue.poll(500, TimeUnit.MILLISECONDS);
                if (task == null) continue;
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
                // Never let the drain loop die due to one bad analyzer
            }
        }
    }

    private void dispatch(StateNode node) {
        for (StateAnalyzer analyzer : analyzers) {
            try {
                analyzer.observe(node);
            } catch (Exception ignored) {
                // Analyzer isolation: one crash cannot break others
            }
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    /** Called from Extension unload handler. */
    public void shutdown() {
        executor.shutdownNow();
    }
}