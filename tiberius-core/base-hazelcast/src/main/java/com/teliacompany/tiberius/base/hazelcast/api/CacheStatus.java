package com.teliacompany.tiberius.base.hazelcast.api;

public class CacheStatus {
    private String name;
    private int numberOfEntries;
    private int timeToLiveSeconds;
    private int maxIdleSeconds;
    private String evictionPolicy;
    private String maxSizePolicy;
    private int maxSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public int getMaxIdleSeconds() {
        return maxIdleSeconds;
    }

    public void setMaxIdleSeconds(int maxIdleSeconds) {
        this.maxIdleSeconds = maxIdleSeconds;
    }

    public String getEvictionPolicy() {
        return evictionPolicy;
    }

    public void setEvictionPolicy(String evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public String getMaxSizePolicy() {
        return maxSizePolicy;
    }

    public void setMaxSizePolicy(String maxSizePolicy) {
        this.maxSizePolicy = maxSizePolicy;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
