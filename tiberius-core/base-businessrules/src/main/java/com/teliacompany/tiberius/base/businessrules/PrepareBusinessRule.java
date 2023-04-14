package com.teliacompany.tiberius.base.businessrules;

import java.util.List;
import java.util.Map;

public interface PrepareBusinessRule<T> extends BusinessRule {
    default boolean shouldBeApplied(T object) {
        return true;
    }

    @Override
    default BusinessRulePhase getPhase() {
        return BusinessRulePhase.PREPARE_PHASE;
    }

    Map<String, T> apply(BusinessRuleEnforcerRequest request, List<T> objects);
}
