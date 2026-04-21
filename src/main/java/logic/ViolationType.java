package logic;

public enum ViolationType {

    STORED_STATE,
    CROSS_SESSION,
    FINANCIAL_CONTEXT,

    // Phase 3
    WORKFLOW_STATE_JUMP,
    WORKFLOW_REPLAY,

    // Phase 4
    FINANCIAL_DELTA,

    // Phase 6D
    TENANT_BREAKOUT
}