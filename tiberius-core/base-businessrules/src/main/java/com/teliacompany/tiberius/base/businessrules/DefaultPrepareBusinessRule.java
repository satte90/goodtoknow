package com.teliacompany.tiberius.base.businessrules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@BusinessRuleComponent(
        summary = "Default Prepare rule, only used if no other PrepareBusinessRule is provided",
        description = "Converts list of I to a map with a UUID string as key"
)
@Conditional(ManualDefaultRuleInstantiation.class)
public class DefaultPrepareBusinessRule<I> implements PrepareBusinessRule<I> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPrepareBusinessRule.class);

    @Override
    public Map<String, I> apply(BusinessRuleEnforcerRequest request, List<I> objects) {
        return objects.stream()
                .collect(Collectors.toMap(this::getUniqueIdForSubscription, a -> a, (a1, a2) -> a1));
    }

    /**
     * All subscriptions should have subscriptionId, but if the data is set up incorrectly and subscriptionId is missing, use a random unique id instead so the
     * subscription can be processed.
     *
     * @return - subscriptionId if it exist, otherwise random unique id
     */
    private String getUniqueIdForSubscription(I a) {
        return UUID.randomUUID().toString();
    }
}
