package com.teliacompany.tiberius.base.businessrules;

import java.util.List;
import java.util.Map;

public interface EndPhaseBusinessRule<T> extends BusinessRule {

    default boolean shouldBeApplied(T object) {
        return true;
    }

    @Override
    default BusinessRulePhase getPhase() {
        return BusinessRulePhase.END_PHASE;
    }

    List<T> apply(BusinessRuleEnforcerRequest request, Map<String, T> resultsMap);
}
