package com.teliacompany.tiberius.base.businessrules.preprocessor;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class PlantUmlCreator {
    private PlantUmlCreator() {
        //Not to be instantiated
    }

    static String createPlantUml(List<BusinessRuleDocument> rules) {
        StringBuilder sb = new StringBuilder("@startuml\n");
        sb.append("'Generated file, do not edit manually\n");
        sb.append("skinparam componentStyle rectangle\n");
        sb.append("skinparam ArrowColor purple\n");
        sb.append("skinparam rectangle {\n" +
                "    FontSize 16\n" +
                "    BackgroundColor DodgerBlue\n" +
                "    FontColor white\n" +
                "    BorderColor #333\n" +
                "    shadowing false\n" +
                "    BorderThickness 2\n" +
                "}\n" +
                "skinparam frame {\n" +
                "    FontSize 22\n" +
                "    FontColor #333\n" +
                "    BorderColor #333\n" +
                "    BackgroundColor #f5f5f5\n" +
                "    shadowing true\n" +
                "}\n");

        rules.stream()
                .filter(rule -> rule.getInterfaceName() == null)
                .filter(rule -> !rule.getDependencies().isEmpty() || rules.stream().anyMatch(rule2 -> rule2.getDependencies().contains(rule.getName())))
                .forEach(rule -> sb.append("component ").append(rule.getName()).append("\n"));

        Set<String> packages = rules.stream()
                .map(BusinessRuleDocument::getInterfaceName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        packages.stream()
                .filter(pack -> !pack.contains("."))
                .forEach(pack -> {
                    StringBuilder packageRulesBuilder = new StringBuilder();
                    packages.stream()
                            .filter(subPack -> StringUtils.startsWith(subPack, pack + "."))
                            .forEach(subPack -> {
                                final String subPackageName = StringUtils.removeStart(subPack, pack + ".");
                                writePackageStart(packageRulesBuilder, subPackageName);
                                writePackageRules(rules, packageRulesBuilder, subPack);
                                writePackageEnd(packageRulesBuilder);
                            });
                    writePackageRules(rules, packageRulesBuilder, pack);

                    if(packageRulesBuilder.length() > 0) {
                        writePackageStart(sb, pack);
                        sb.append(packageRulesBuilder);
                        writePackageEnd(sb);
                    }
                });

        sb.append("\n");
        rules.forEach(rule -> rule.getDependencies()
                .forEach(d -> sb.append(rule.getName()).append("---|>").append(d).append("\n")));

        sb.append("\n").append("@enduml");

        return sb.toString();
    }

    private static void writePackageStart(StringBuilder sb, String pack) {
        sb.append("frame \"").append(pack).append("\" {").append("\n");
    }

    private static void writePackageRules(List<BusinessRuleDocument> rules, StringBuilder sb, String packageName) {
        rules.stream()
                .filter(rule -> packageName.equals(rule.getInterfaceName()))
                .filter(rule -> !rule.getDependencies().isEmpty() || rules.stream().anyMatch(rule2 -> rule2.getDependencies().contains(rule.getName())))
                .forEach(rule -> sb.append("component ").append(rule.getName()).append("\n"));

    }

    private static void writePackageEnd(StringBuilder sb) {
        sb.append("}").append("\n");
    }
}
