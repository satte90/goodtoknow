package com.teliacompany.tiberius.base.businessrules;

import com.teliacompany.tiberius.base.businessrules.BusinessRuleMetaData.Entry;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"UnusedReturnValue"})
public class BusinessRuleMetaData extends HashMap<Class<? extends BusinessRule>, Entry> {
    public Entry putNewEntry(Class<? extends BusinessRule> aClass, Entry entry) {
        return super.put(aClass, entry);
    }

    public boolean applyForRequestType(BusinessRule businessRule, RequestTypeEnum requestTypeEnum) {
        Class<? extends BusinessRule> businessRuleClass = businessRule.getClass();
        Entry entry = get(businessRuleClass);
        if(entry == null || requestTypeEnum == null) {
            return true;
        }
        if(!entry.allowList.isEmpty()) {
            return entry.allowList.contains(requestTypeEnum.name());
        }
        return !entry.denyList.contains(requestTypeEnum.name());
    }

    public List<Class<? extends BusinessRule>> getDependenciesFor(BusinessRule rule) {
        return get(rule.getClass()).dependencies;
    }

    public static class Entry {
        private List<Class<? extends BusinessRule>> dependencies = new ArrayList<>();
        private List<String> allowList = new ArrayList<>();
        private List<String> denyList = new ArrayList<>();
        private String summary;
        private String description;
        private String appliedWhenInfo;

        public Entry setDependencies(List<Class<? extends BusinessRule>> dependencies) {
            this.dependencies = Objects.requireNonNullElseGet(dependencies, ArrayList::new);
            return this;
        }

        public Entry setAllowList(List<String> allowList) {
            this.allowList = Objects.requireNonNullElseGet(allowList, ArrayList::new);
            return this;
        }

        public Entry setDenyList(List<String> denyList) {
            this.denyList = Objects.requireNonNullElseGet(denyList, ArrayList::new);
            return this;
        }

        public Entry setSummary(String summary) {
            this.summary = Objects.requireNonNullElseGet(summary, String::new);
            return this;
        }

        public Entry setDescription(String description) {
            this.description = Objects.requireNonNullElseGet(description, String::new);
            return this;
        }

        public Entry setAppliedWhenInfo(String appliedWhenInfo) {
            this.appliedWhenInfo = Objects.requireNonNullElseGet(appliedWhenInfo, String::new);
            return this;
        }

        @NonNull
        public List<Class<? extends BusinessRule>> getDependencies() {
            return dependencies;
        }

        @NonNull
        public List<String> getAllowList() {
            return allowList;
        }

        @NonNull
        public List<String> getDenyList() {
            return denyList;
        }

        @NonNull
        public String getSummary() {
            return summary;
        }

        @NonNull
        public String getDescription() {
            return description;
        }

        @NonNull
        public String getAppliedWhenInfo() {
            return appliedWhenInfo;
        }
    }
}
