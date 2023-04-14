package com.teliacompany.tiberius.base.mongodb.converter;

import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.tiberius.base.mongodb.model.QueryDateType;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

public final class QueryDateConverter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private QueryDateConverter() {
        //Not to be instantiated
    }

    @NonNull
    public static Object parseDateAs(String date, QueryDateType dateType) {
        switch(dateType) {
            case LONG:
                if(NumberUtils.isParsable(date)) {
                    return Long.parseLong(date);
                }
                return Instant.parse(date).toEpochMilli();
            case INSTANT:
                return Instant.parse(date);
            default:
            case DATE:
                try {
                    return DATE_FORMAT.parse(date);
                } catch(ParseException e) {
                    throw new BadRequestException("Provided date cannot be parsed using format yyy-MM-dd");
                }
        }
    }
}
