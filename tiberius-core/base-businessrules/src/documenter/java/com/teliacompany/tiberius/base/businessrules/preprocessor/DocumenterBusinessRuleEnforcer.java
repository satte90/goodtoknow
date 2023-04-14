package com.teliacompany.tiberius.base.businessrules.preprocessor;

import com.teliacompany.tiberius.base.businessrules.BusinessRule;
import com.teliacompany.tiberius.base.businessrules.BusinessRuleEnforcer;
import com.teliacompany.tiberius.base.businessrules.BusinessRuleMetaData;
import com.teliacompany.tiberius.base.businessrules.EndPhaseBusinessRule;
import com.teliacompany.tiberius.base.businessrules.InitialBusinessRule;
import com.teliacompany.tiberius.base.businessrules.MainBusinessRule;
import com.teliacompany.tiberius.base.businessrules.PrepareBusinessRule;
import com.teliacompany.tiberius.base.businessrules.SetupBusinessRule;

import java.util.List;

public class DocumenterBusinessRuleEnforcer<I, T> extends BusinessRuleEnforcer<I, T> {
    protected DocumenterBusinessRuleEnforcer(List<BusinessRule> businessRules) {
        super(businessRules);
    }

    public BusinessRuleMetaData getBusinessRuleMetaData() {
        return super.getBusinessRuleMetaData();
    }

    public InitialBusinessRule<I, T> getInitialBusinessRule() {
        return super.getInitialBusinessRule();
    }

    public List<SetupBusinessRule<T>> getSetupBusinessRules() {
        return super.getSetupBusinessRules();
    }

    public PrepareBusinessRule<T> getPrepareBusinessRule() {
        return super.getPrepareBusinessRule();
    }

    public List<MainBusinessRule<T>> getMainBusinessRules() {
        return super.getMainBusinessRules();
    }

    public EndPhaseBusinessRule<T> getEndPhaseBusinessRule() {
        return super.getEndPhaseBusinessRule();
    }
}
