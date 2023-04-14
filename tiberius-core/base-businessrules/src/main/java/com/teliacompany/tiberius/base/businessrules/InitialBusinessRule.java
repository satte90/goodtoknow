package com.teliacompany.tiberius.base.businessrules;

import java.util.List;

public interface InitialBusinessRule<I, T> extends BusinessRule {

    default boolean shouldBeApplied(T object) {
        return true;
    }

    @Override
    default BusinessRulePhase getPhase() {
        return BusinessRulePhase.INITIAL_PHASE;
    }

    List<T> apply(I object);
}
