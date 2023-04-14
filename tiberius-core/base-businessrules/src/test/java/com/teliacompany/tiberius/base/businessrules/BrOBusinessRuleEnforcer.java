package com.teliacompany.tiberius.base.businessrules;

import java.util.List;

public class BrOBusinessRuleEnforcer extends BusinessRuleEnforcer<Brobject, Brobject> {
    protected BrOBusinessRuleEnforcer(List<BusinessRule> businessRules) {
        super(businessRules);
    }
}
