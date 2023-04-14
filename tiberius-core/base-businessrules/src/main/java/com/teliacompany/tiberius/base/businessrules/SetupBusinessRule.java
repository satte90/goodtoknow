package com.teliacompany.tiberius.base.businessrules;

public interface SetupBusinessRule<T> extends BusinessRule {
    default boolean shouldBeApplied(T object) {
        return true;
    }

    @Override
    default BusinessRulePhase getPhase() {
        return BusinessRulePhase.SETUP_PHASE;
    }

    void apply(T object, BusinessRuleEnforcerRequest ruleRequest);
}
