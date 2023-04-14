package com.teliacompany.tiberius.base.hazelcast.exception;

public class HazelcastMissingChartPropertiesException extends RuntimeException {
    public HazelcastMissingChartPropertiesException() {
        super("Missing Hazelcast properties in chart/values file. See https://diva.teliacompany.net/confluence/x/oc--CQ for more info.");
    }
}
