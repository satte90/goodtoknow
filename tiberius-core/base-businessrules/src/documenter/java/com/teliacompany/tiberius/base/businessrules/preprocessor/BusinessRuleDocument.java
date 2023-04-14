package com.teliacompany.tiberius.base.businessrules.preprocessor;

import java.util.ArrayList;
import java.util.List;

public class BusinessRuleDocument {
    private String name;
    private String summary;
    private String applyWhen;
    private String description;
    private String interfaceName;
    private List<String> dependencies = new ArrayList<>();
    private List<String> applyOnlyForRequests = new ArrayList<>();
    private List<String> dontApplyForRequests = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public List<String> getApplyOnlyForRequests() {
        return applyOnlyForRequests;
    }

    public List<String> getDontApplyForRequests() {
        return dontApplyForRequests;
    }

    public String getSummary() {
        return summary;
    }

    public String getApplyWhen() {
        return applyWhen;
    }

    public BusinessRuleDocument setName(String name) {
        this.name = name;
        return this;
    }

    public BusinessRuleDocument setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public BusinessRuleDocument setApplyWhen(String applyWhen) {
        this.applyWhen = applyWhen;
        return this;
    }

    public BusinessRuleDocument setDescription(String description) {
        this.description = description;
        return this;
    }

    public BusinessRuleDocument setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public BusinessRuleDocument setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public BusinessRuleDocument setApplyOnlyForRequests(List<String> applyOnlyForRequests) {
        this.applyOnlyForRequests = applyOnlyForRequests;
        return this;
    }

    public BusinessRuleDocument setDontApplyForRequests(List<String> dontApplyForRequests) {
        this.dontApplyForRequests = dontApplyForRequests;
        return this;
    }
}
