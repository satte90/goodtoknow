package com.teliacompany.tiberius.base.mongodb.model;

public class QueryRequest {
    private final String fieldName;
    private final String date;
    private final QueryDateType dateType;
    private final Integer limit;

    public QueryRequest(String fieldName, String date, QueryDateType dateType, Integer limit) {
        this.fieldName = fieldName;
        this.date = date;
        this.dateType = dateType;
        this.limit = limit;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDate() {
        return date;
    }

    public QueryDateType getDateType() {
        return dateType;
    }

    public Integer getLimit() {
        return limit;
    }
}
