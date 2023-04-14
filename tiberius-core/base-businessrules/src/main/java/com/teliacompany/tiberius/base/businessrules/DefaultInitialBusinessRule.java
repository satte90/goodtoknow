package com.teliacompany.tiberius.base.businessrules;

import org.springframework.context.annotation.Conditional;

import java.util.List;

@BusinessRuleComponent(
        summary = "Default Initial rule, only used if no other InitialBusinessRule is provided",
        description = "Does nothing, just returns the input list as is"
)
@Conditional(ManualDefaultRuleInstantiation.class)
public class DefaultInitialBusinessRule<T> implements InitialBusinessRule<T, T> {

    @Override
    public List<T> apply(T object) {
        return List.of(object);
    }
}
