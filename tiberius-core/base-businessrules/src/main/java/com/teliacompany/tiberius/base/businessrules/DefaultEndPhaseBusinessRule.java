package com.teliacompany.tiberius.base.businessrules;

import org.springframework.context.annotation.Conditional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@BusinessRuleComponent(
        summary = "Default final phase business rule, only used if no other EndPhaseBusinessRule is provided",
        description = "Collect map of objects into a list",
        appliedWhenInfo = "Rule is applied in the end after all the business rules"
)
@Conditional(ManualDefaultRuleInstantiation.class)
public class DefaultEndPhaseBusinessRule<T> implements EndPhaseBusinessRule<T> {

    @Override
    public List<T> apply(BusinessRuleEnforcerRequest request, Map<String, T> resultsMap) {
        return new ArrayList<>(resultsMap.values());
    }

}
