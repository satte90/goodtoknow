package com.teliacompany.webflux.request.filter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonPayloadLoggingFilter {

    private static final Pattern EMPTY_PATTERN = compilePattern(buildRegex(Collections.emptyList()));

    public enum FilterFunction {
        CONTAINS, EXACTLY, STARTS_WITH, ENDS_WITH, STARTS_OR_ENDS_WITH
    }

    private Pattern filterPattern = EMPTY_PATTERN;
    private final List<String> forbiddenKeys = new ArrayList<>();

    public static JsonPayloadLoggingFilter empty() {
        return new JsonPayloadLoggingFilter();
    }

    public static JsonPayloadLoggingFilter defaultFilter() {
        return new JsonPayloadLoggingFilter()
                .add(FilterFunction.CONTAINS, "password")
                .add(FilterFunction.STARTS_OR_ENDS_WITH, "secret")
                .add(FilterFunction.EXACTLY, "psw")
                .add(FilterFunction.EXACTLY, "pwd")
                .add(FilterFunction.EXACTLY, "pass")
                .add(FilterFunction.CONTAINS, "passord")
                .add(FilterFunction.CONTAINS, "lösenord");
    }

    public JsonPayloadLoggingFilter add(String regex) {
        forbiddenKeys.add(regex);
        filterPattern = compilePattern(getFullRegex());
        return this;
    }

    public JsonPayloadLoggingFilter add(FilterFunction filterFunction, String regex) {
        switch(filterFunction) {
            case CONTAINS:
                forbiddenKeys.add(contains(regex));
                break;
            case STARTS_WITH:
                forbiddenKeys.add(startsWith(regex));
                break;
            case ENDS_WITH:
                forbiddenKeys.add(endsWith(regex));
                break;
            case STARTS_OR_ENDS_WITH:
                forbiddenKeys.add(startsOrEndsWith(regex));
                break;
            case EXACTLY:
            default:
                forbiddenKeys.add(exactly(regex));
        }
        filterPattern = compilePattern(getFullRegex());
        return this;
    }

    public byte[] filterPayload(byte[] payload) {
        if(forbiddenKeys.isEmpty()) {
            return payload;
        }
        return filterPayload(new String(payload, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    public String filterPayload(String payload) {
        if(forbiddenKeys.isEmpty()) {
            return payload;
        }

        Matcher m = filterPattern.matcher(payload);
        if(m.find()) {
            return m.replaceAll("\"$1\":\"*************\"");
        }
        return payload;
    }

    public String getFullRegex() {
        return buildRegex(forbiddenKeys);
    }

    private static String buildRegex(List<String> forbiddenKeys) {
        return "\"(" + String.join("|", forbiddenKeys) + ")\"\\s*:\\s*\"([^\"]*)\"";
    }

    public List<String> getForbiddenKeys() {
        return forbiddenKeys;
    }

    private static Pattern compilePattern(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    private static String contains(String password) {
        return "[^\"]*" + password + "[^\"]*";
    }

    private static String startsOrEndsWith(String password) {
        return startsWith(password) + "|" + endsWith(password);
    }

    private static String startsWith(String password) {
        return password + "[^\"]*";
    }

    private static String endsWith(String password) {
        return "[^\"]*" + password;
    }

    private static String exactly(String password) {
        return password;
    }
}
