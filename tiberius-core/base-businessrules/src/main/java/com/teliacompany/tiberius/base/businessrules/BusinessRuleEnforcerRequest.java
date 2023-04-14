package com.teliacompany.tiberius.base.businessrules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Request object sent to setUp business rules. Contains the requestData as well as various custom data that will differ
 * depending on request type.
 * Custom Data can only be added by providing a Class

 * Consumers of this class does not need to know anything about the internal data storage mechanism, simply use:
 * - put(Class<T> type, T object)
 * - put(Class<T> type, List<T> objects)
 * - get(Class<T> type)
 * - getAll(Class<T> type)
 *
 * Tip: Define static variables for the classes you which to store, e.g. public static Class<ParsedResult> MAGNOLIA_DATA.
 */
public class BusinessRuleEnforcerRequest {
    private final RequestTypeEnum requestType;
    private final Map<Class<?>, List<Object>> customData = new HashMap<>();

    public BusinessRuleEnforcerRequest(RequestTypeEnum requestType) {
        this.requestType = requestType;
    }

    public RequestTypeEnum getRequestType() {
        return requestType;
    }

    public <T> BusinessRuleEnforcerRequest put(Class<T> dataType, T data) {
        return put(dataType, Collections.singletonList(data));
    }

    public <T> BusinessRuleEnforcerRequest put(Class<T> dataType, List<T> dataList) {
        if(!customData.containsKey(dataType)) {
            customData.put(dataType, new ArrayList<>());
        }

        if(dataList != null) {
            customData.get(dataType).addAll(dataList);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> dataType) {
        List<Object> list = Optional.ofNullable(customData.get(dataType)).orElseGet(Collections::emptyList);
        if(list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) list.get(0));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(Class<T> dataType) {
        List<Object> list = Optional.ofNullable(customData.get(dataType)).orElseGet(Collections::emptyList);
        return list.stream().map(o -> (T) o).collect(Collectors.toList());
    }
}
