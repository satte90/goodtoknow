package com.teliacompany.tiberius.base.businessrules;

import java.util.Map;

public interface MainBusinessRule<T> extends BusinessRule {

    boolean shouldBeApplied(T object);

    @Override
    default BusinessRulePhase getPhase() {
        return BusinessRulePhase.MAIN_PHASE;
    }

    void apply(T rootObject, Map<String, T> resultsMap);
}
