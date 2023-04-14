package com.teliacompany.tiberius.base.businessrules.preprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MarkdownCreator {

    public static final String SUP_TAG = "<sup>";

    private MarkdownCreator() {
        //Not to be instantiated
    }

    static String createMd(List<BusinessRuleDocument> rules) {
        StringBuilder sb = new StringBuilder("[//]: # \"Generated File, do not edit manually\"\n");
        sb.append("#Business Rules\n");

        rules.forEach(rule -> {
            sb.append("## ").append(rule.getName()).append("\n");
            sb.append("**").append(rule.getSummary()).append("**").append("\n").append("\n");

            if(!"N/A".equalsIgnoreCase(rule.getDescription())) {
                sb.append(rule.getDescription()).append("\n");
                sb.append("\n");
            }

            sb.append("<sup>**Applied when:** ").append(rule.getApplyWhen()).append(SUP_TAG).append("\n").append("\n");

            if(!rule.getApplyOnlyForRequests().isEmpty()) {
                sb.append(SUP_TAG).append("**Only applied for:** ").append(rule.getApplyOnlyForRequests()).append(SUP_TAG);
                sb.append("\n\n");
            }

            if(!rule.getDontApplyForRequests().isEmpty()) {
                sb.append(SUP_TAG).append("**Never applied for:** ").append(rule.getDontApplyForRequests()).append(SUP_TAG);
                sb.append("\n\n");
            }

            sb.append(SUP_TAG).append("**Dependencies:** ");
            if(rule.getDependencies().isEmpty()) {
                sb.append("```none```").append("\n");
            } else {
                List<String> dependencies = new ArrayList<>(rule.getDependencies());
                String dependenciesString = dependencies.stream()
                        .map(d -> "[```" + d + "```](#" + d + ")")
                        .collect(Collectors.joining(", "));
                sb.append(dependenciesString).append(SUP_TAG).append("\n");
            }
        });

        return sb.toString();
    }
}
