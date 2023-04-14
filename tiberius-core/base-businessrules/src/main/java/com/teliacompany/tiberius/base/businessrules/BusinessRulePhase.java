package com.teliacompany.tiberius.base.businessrules;

public enum BusinessRulePhase {
    INITIAL_PHASE(0), SETUP_PHASE(1), PREPARE_PHASE(2), MAIN_PHASE(3), END_PHASE(5);

    private final int priority;

    BusinessRulePhase(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
