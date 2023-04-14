package com.teliacompany.tiberius.base.businessrules;

import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.log.trace.InactiveTraceLogger;
import com.teliacompany.webflux.request.log.trace.TraceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BusinessRuleEnforcer<I, T> {
    private static final Logger STARTUP_LOG = LoggerFactory.getLogger("com.teliacompany.tiberius.startup.Logger");
    private static final InactiveTraceLogger INACTIVE_TRACE_LOGGER = new InactiveTraceLogger();

    protected final BusinessRuleMetaData businessRuleMetaData;
    protected final InitialBusinessRule<I, T> initialBusinessRule;
    protected final List<SetupBusinessRule<T>> setupBusinessRules;
    protected final PrepareBusinessRule<T> prepareBusinessRule;
    protected final List<MainBusinessRule<T>> mainBusinessRules;
    protected final EndPhaseBusinessRule<T> endPhaseBusinessRule;

    protected BusinessRuleEnforcer(List<BusinessRule> businessRules) {
        this.businessRuleMetaData = collectMetaData(businessRules);
        this.initialBusinessRule = findInitialBusinessRule(businessRules);
        this.prepareBusinessRule = findPrepareBusinessRule(businessRules);
        this.setupBusinessRules = findAndSortSetupBusinessRules(businessRules);
        this.mainBusinessRules = findAndSortMainBusinessRules(businessRules);
        this.endPhaseBusinessRule = findEndPhaseBusinessRule(businessRules);

        if(STARTUP_LOG.isInfoEnabled()) {
            STARTUP_LOG.info("Sorted businessRules: \n{}\n{}\n{}\n{}\n{}",
                    this.initialBusinessRule.getClass().getSimpleName(),
                    this.setupBusinessRules.stream()
                            .map(rule -> rule.getPhase() + " " + rule.getClass().getSimpleName())
                            .collect(Collectors.joining("\n")),
                    this.prepareBusinessRule.getClass().getSimpleName(),
                    this.mainBusinessRules.stream()
                            .map(rule -> rule.getPhase() + " " + rule.getClass().getSimpleName())
                            .collect(Collectors.joining("\n")),
                    this.endPhaseBusinessRule.getClass().getSimpleName()
            );
        }
    }

    public Mono<List<T>> applyBusinessRules(BusinessRuleEnforcerRequest request, Collection<I> input) {
        return RequestProcessor.getTraceLogger()
                .map(traceLogger -> {
                    traceLogger.addLogEntry("Applying initial business rule on {} objects", input.size());
                    final List<T> processedInputs = applyInitialBusinessRule(input);

                    traceLogger.addLogEntry("Applying setup rules on {} objects...", processedInputs.size());
                    applySetUpBusinessRules(request, processedInputs, traceLogger);

                    traceLogger.addLogEntry("Applying prepare rule on {} objects...", processedInputs.size());
                    final Map<String, T> resultsMap = applyPrepareBusinessRule(request, processedInputs);

                    traceLogger.addLogEntry("Applying main business rules on {} objects...", resultsMap.size());
                    applyMainBusinessRules(request, resultsMap, traceLogger);

                    traceLogger.addLogEntry("Applying end phase business rule on {} objects...", resultsMap.size());
                    final List<T> result = applyEndPhaseBusinessRule(request, resultsMap);

                    traceLogger.addLogEntry("Applied all business rules, there are a total of {} objects...", result.size());
                    return result;
                });
    }

    protected List<T> applyInitialBusinessRule(Collection<I> input) {
        return input.stream()
                .flatMap(object -> initialBusinessRule.apply(object).stream())
                .collect(Collectors.toList());
    }

    protected void applySetUpBusinessRules(BusinessRuleEnforcerRequest request, List<T> objects, TraceLogger traceLogger) {
        setupBusinessRules.stream()
                .filter(rule -> this.businessRuleMetaData.applyForRequestType(rule, request.getRequestType()))
                .forEach(setupRule -> {
                    final String businessRuleName = setupRule.getClass().getSimpleName();
                    traceLogger.addLogEntry("Applying businessRule: {} on {} objects", businessRuleName, objects.size());
                    objects.forEach(object -> setupRule.apply(object, request));
                });
    }

    protected Map<String, T> applyPrepareBusinessRule(BusinessRuleEnforcerRequest enforcerRequest, List<T> processedInputs) {
        return prepareBusinessRule.apply(enforcerRequest, processedInputs);
    }

    protected void applyMainBusinessRules(BusinessRuleEnforcerRequest request, Map<String, T> resultsMap) {
        applyMainBusinessRules(request, resultsMap, INACTIVE_TRACE_LOGGER);
    }

    protected void applyMainBusinessRules(BusinessRuleEnforcerRequest request, Map<String, T> resultsMap, TraceLogger traceLogger) {
        mainBusinessRules.stream()
                .filter(rule -> this.businessRuleMetaData.applyForRequestType(rule, request.getRequestType()))
                .forEach(rule -> {
                    //Before each rule, update the ruleIds as the resultsMap may have been updated
                    Set<String> ruleIds2 = new HashSet<>(resultsMap.keySet()); //Get a copy of the keys

                    final String businessRuleName = rule.getClass().getSimpleName();
                    traceLogger.addLogEntry("Applying businessRule: {} on {} objects", businessRuleName, ruleIds2.size());

                    ruleIds2.forEach(ruleId -> {
                        T ruleObject = resultsMap.get(ruleId);
                        if(ruleObject != null) {
                            applyRule(resultsMap, rule, ruleObject);
                        }
                    });
                });
    }

    /**
     * Possible to override this if you need to do something special generic logic on when applying rules
     */
    protected void applyRule(Map<String, T> resultsMap, MainBusinessRule<T> rule, T ruleObject) {
        if(rule.shouldBeApplied(ruleObject)) {
            rule.apply(ruleObject, resultsMap);
        }
    }

    protected List<T> applyEndPhaseBusinessRule(BusinessRuleEnforcerRequest enforcerRequest, Map<String, T> resultsMap) {
        return endPhaseBusinessRule.apply(enforcerRequest, resultsMap);
    }

    protected BusinessRuleMetaData getBusinessRuleMetaData() {
        return businessRuleMetaData;
    }

    protected InitialBusinessRule<I, T> getInitialBusinessRule() {
        return initialBusinessRule;
    }

    protected List<SetupBusinessRule<T>> getSetupBusinessRules() {
        return setupBusinessRules;
    }

    protected PrepareBusinessRule<T> getPrepareBusinessRule() {
        return prepareBusinessRule;
    }

    protected List<MainBusinessRule<T>> getMainBusinessRules() {
        return mainBusinessRules;
    }

    protected EndPhaseBusinessRule<T> getEndPhaseBusinessRule() {
        return endPhaseBusinessRule;
    }

    private BusinessRuleMetaData collectMetaData(List<BusinessRule> businessRules) {
        BusinessRuleMetaData metaData = new BusinessRuleMetaData();
        businessRules.forEach(businessRule -> {
            BusinessRuleComponent annotation = businessRule.getClass().getAnnotation(BusinessRuleComponent.class);
            List<Class<? extends BusinessRule>> dependencies = getDependencies(annotation);

            List<String> allowList = Arrays.asList(annotation.applyOnlyForRequests());
            List<String> denyList = Arrays.asList(annotation.dontApplyForRequests());

            BusinessRuleMetaData.Entry entry = new BusinessRuleMetaData.Entry()
                    .setSummary(annotation.summary())
                    .setDescription(annotation.description())
                    .setAppliedWhenInfo(annotation.appliedWhenInfo())
                    .setAllowList(allowList)
                    .setDenyList(denyList)
                    .setDependencies(dependencies);

            metaData.putNewEntry(businessRule.getClass(), entry);
        });
        return metaData;
    }

    private List<Class<? extends BusinessRule>> getDependencies(BusinessRuleComponent annotation) {
        Class<? extends BusinessRule>[] dependencies = annotation.dependsOn();
        return Arrays.stream(dependencies)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private InitialBusinessRule<I, T> findInitialBusinessRule(List<BusinessRule> genericBusinessRules) {
        return genericBusinessRules.stream()
                .filter(rule -> rule instanceof InitialBusinessRule)
                .map(InitialBusinessRule.class::cast)
                .findFirst()
                .orElseGet(DefaultInitialBusinessRule::new);
    }

    @SuppressWarnings("unchecked")
    private PrepareBusinessRule<T> findPrepareBusinessRule(List<BusinessRule> genericBusinessRules) {
        return genericBusinessRules.stream()
                .filter(rule -> rule instanceof PrepareBusinessRule)
                .map(PrepareBusinessRule.class::cast)
                .findFirst()
                .orElseGet(DefaultPrepareBusinessRule::new);
    }

    @SuppressWarnings("unchecked")
    private EndPhaseBusinessRule<T> findEndPhaseBusinessRule(List<BusinessRule> genericBusinessRules) {
        return genericBusinessRules.stream()
                .filter(rule -> rule instanceof EndPhaseBusinessRule)
                .map(EndPhaseBusinessRule.class::cast)
                .findFirst()
                .orElseGet(DefaultEndPhaseBusinessRule::new);
    }

    @SuppressWarnings("unchecked")
    private List<MainBusinessRule<T>> findAndSortMainBusinessRules(List<BusinessRule> genericBusinessRules) {
        List<MainBusinessRule<T>> businessRules = new ArrayList<>();
        genericBusinessRules.stream()
                .filter(rule -> rule instanceof MainBusinessRule)
                .forEach(rule -> businessRules.add((MainBusinessRule<T>) rule));
        this.sortBusinessRules(businessRules);
        return businessRules;
    }

    @SuppressWarnings("unchecked")
    private List<SetupBusinessRule<T>> findAndSortSetupBusinessRules(List<BusinessRule> genericBusinessRules) {
        List<SetupBusinessRule<T>> businessRules = new ArrayList<>();
        genericBusinessRules.stream()
                .filter(rule -> rule instanceof SetupBusinessRule)
                .forEach(rule -> businessRules.add((SetupBusinessRule<T>) rule));
        this.sortBusinessRules(businessRules);
        return businessRules;
    }

    /*
        Sort businessRules by dependencies. This is a lazy topological sort... Simply loop businessRules, remove it form the queue and add them to a sorted list
        if all dependent rules are already added to the sorted list. Otherwise put the rule in the back of the queue again. Run until queue is empty or break when we
        have iterated the queue for too long, that _probably_ means we have a cyclic dependency.
    */
    @SuppressWarnings("unchecked")
    private <RULE extends BusinessRule> void sortBusinessRules(List<RULE> businessRules) {
        int index = 0;
        final int maxIterations = businessRules.size() * 2 + 1;

        final List<RULE> businessRulesQueue = new ArrayList<>(businessRules);
        businessRules.clear();

        while(!businessRulesQueue.isEmpty()) {
            RULE rule = businessRulesQueue.remove(0);
            final boolean allDependenciesAlreadyInResultList = businessRuleMetaData.getDependenciesFor(rule).stream()
                    .allMatch(dep -> {
                        //If dependency is an interface such as PriceBusinessRules, make sure all rules of that class has been processed and removed from queue first
                        if(dep.isInterface()) {
                            return businessRulesQueue.stream().noneMatch(unsortedRule -> dep.isAssignableFrom(unsortedRule.getClass()));
                        }
                        return businessRules.stream().anyMatch(sortedRule -> sortedRule.getClass().isAssignableFrom(dep));
                    });
            if(allDependenciesAlreadyInResultList) {
                businessRules.add(rule);
                index = 0;
            } else {
                businessRulesQueue.add(rule);
                index++;
            }

            // Hacky, not totally reliable and lazy way of detecting dependency cycles :)
            if(index > maxIterations) {
                logSortingError(businessRulesQueue, maxIterations, businessRules);
                throw new InternalServerErrorException("Probable business rule dependency cycle detected!");
            }
        }
    }

    private <RULE extends BusinessRule> void logSortingError(List<RULE> businessRulesQueue, int maxIterations, List<RULE> sortedRules) {
        STARTUP_LOG.error("\nCan't sort the following business rules, exceeded max iterations ({}):", maxIterations);
        businessRulesQueue.forEach(rule -> {
            BusinessRuleComponent annotation = rule.getClass().getAnnotation(BusinessRuleComponent.class);
            if(annotation != null) {
                final String dependencies = Arrays.stream(annotation.dependsOn())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining("\n\t"));
                STARTUP_LOG.error("{} has dependency on \n\t{}", rule.getClass().getSimpleName(), dependencies);
            } else {
                STARTUP_LOG.error("{} has no BusinessRuleComponent \n", rule.getClass().getSimpleName());
            }
        });
        STARTUP_LOG.error("Is there a cyclic dependency?\n\n");

        final String sortedRulesLog = sortedRules.stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining("\n\t"));
        STARTUP_LOG.info("Sorted rules: \n{}", sortedRulesLog);
    }
}
