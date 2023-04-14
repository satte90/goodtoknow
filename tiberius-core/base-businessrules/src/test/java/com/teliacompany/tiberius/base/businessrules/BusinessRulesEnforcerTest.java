package com.teliacompany.tiberius.base.businessrules;

import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleA;
import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleB;
import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleC;
import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleD;
import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleE;
import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleF;
import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleG;
import com.teliacompany.tiberius.base.businessrules.TestBusinessRules.BusinessRuleH;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BusinessRulesEnforcerTest {

    @Test
    public void testCrazySorting1() throws Exception {
        final List<BusinessRule> testRules = new ArrayList<>();
        testRules.add(new BusinessRuleA());
        testRules.add(new BusinessRuleB());
        testRules.add(new BusinessRuleC());
        testRules.add(new BusinessRuleD());
        testRules.add(new BusinessRuleE());
        testRules.add(new BusinessRuleF());
        testRules.add(new BusinessRuleG());
        testRules.add(new BusinessRuleH());

        BrOBusinessRuleEnforcer businessRulesEnforcer = new BrOBusinessRuleEnforcer(new ArrayList<>(testRules));
        List<MainBusinessRule<Brobject>> sortedWrapperRules = businessRulesEnforcer.getMainBusinessRules();

        assertEquals(testRules.size(), sortedWrapperRules.size());

        assertOrderIsOk(sortedWrapperRules);

        assertEquals(BusinessRuleB.class, sortedWrapperRules.get(0).getClass());
        assertEquals(BusinessRuleE.class, sortedWrapperRules.get(1).getClass());
        assertEquals(BusinessRuleH.class, sortedWrapperRules.get(2).getClass());
        assertEquals(BusinessRuleG.class, sortedWrapperRules.get(3).getClass());
        assertEquals(BusinessRuleD.class, sortedWrapperRules.get(4).getClass());
        assertEquals(BusinessRuleA.class, sortedWrapperRules.get(5).getClass());
        assertEquals(BusinessRuleC.class, sortedWrapperRules.get(6).getClass());
        assertEquals(BusinessRuleF.class, sortedWrapperRules.get(7).getClass());
    }

    private static void assertOrderIsOk(List<MainBusinessRule<Brobject>> sortedRules) {
        List<Class<? extends BusinessRule>> visitedClasses = new ArrayList<>();
        sortedRules.forEach(businessRule -> {
            final BusinessRuleComponent annotation = businessRule.getClass().getAnnotation(BusinessRuleComponent.class);
            List<Class<? extends BusinessRule>> dependencies = Arrays.asList(annotation.dependsOn());
            boolean ok = dependencies.stream().allMatch(dependencyClass -> visitedClasses.stream().anyMatch(dependencyClass::equals));
            assertTrue(ok);
            visitedClasses.add(businessRule.getClass());
        });
    }
}
