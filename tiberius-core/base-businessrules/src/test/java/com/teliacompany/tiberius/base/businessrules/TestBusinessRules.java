package com.teliacompany.tiberius.base.businessrules;

import java.util.Map;

public final class TestBusinessRules {
    private TestBusinessRules() {
        //Not to be instantiated
    }

    public static abstract class TestBusinessRule implements MainBusinessRule<Brobject> {
        @Override
        public boolean shouldBeApplied(Brobject Brobject) {
            return true;
        }

        @Override
        public void apply(Brobject Brobject, Map<String, Brobject> resultsMap) {
            // Do nothing
        }

    }

    @BusinessRuleComponent(summary = "", description = "", dependsOn = {BusinessRuleD.class, BusinessRuleG.class})
    public static class BusinessRuleA extends TestBusinessRule {

    }

    @BusinessRuleComponent(summary = "", description = "")
    public static class BusinessRuleB extends TestBusinessRule {

    }

    @BusinessRuleComponent(summary = "", description = "", dependsOn = BusinessRuleD.class)
    public static class BusinessRuleC extends TestBusinessRule {

    }

    @BusinessRuleComponent(summary = "", description = "", dependsOn = {BusinessRuleG.class, BusinessRuleH.class})
    public static class BusinessRuleD extends TestBusinessRule {

    }

    @BusinessRuleComponent(summary = "", description = "", dependsOn = BusinessRuleB.class)
    public static class BusinessRuleE extends TestBusinessRule {

    }

    @BusinessRuleComponent(summary = "", description = "", dependsOn = BusinessRuleC.class)
    public static class BusinessRuleF extends TestBusinessRule {

    }

    @BusinessRuleComponent(summary = "", description = "", dependsOn = BusinessRuleH.class)
    public static class BusinessRuleG extends TestBusinessRule {

    }

    @BusinessRuleComponent(summary = "", description = "")
    public static class BusinessRuleH extends TestBusinessRule {

    }
}
