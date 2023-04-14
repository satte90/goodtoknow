package com.teliacompany.tiberius.base.hazelcast.config;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizePolicy;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastComponent;

public class TiberiusCacheProperties {
    private final int timeToLiveSeconds;
    private final int maxIdleSeconds;
    private final EvictionPolicy evictionPolicy;
    private final MaxSizePolicy maxSizePolicy;
    private final int maxSize;

    public TiberiusCacheProperties(TiberiusHazelcastComponent annotation) {
        this.timeToLiveSeconds = Integer.parseInt(annotation.timeToLiveSeconds());
        this.maxIdleSeconds = Integer.parseInt(annotation.maxIdleSeconds());
        this.evictionPolicy = annotation.evictionPolicy();
        this.maxSizePolicy = annotation.maxSizePolicy();
        this.maxSize = Integer.parseInt(annotation.maxSize());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        TiberiusCacheProperties that = (TiberiusCacheProperties) o;

        if(timeToLiveSeconds != that.timeToLiveSeconds) return false;
        if(maxIdleSeconds != that.maxIdleSeconds) return false;
        if(maxSize != that.maxSize) return false;
        if(evictionPolicy != that.evictionPolicy) return false;
        return maxSizePolicy == that.maxSizePolicy;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public int getMaxIdleSeconds() {
        return maxIdleSeconds;
    }

    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    public MaxSizePolicy getMaxSizePolicy() {
        return maxSizePolicy;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public int hashCode() {
        int result = timeToLiveSeconds;
        result = 31 * result + maxIdleSeconds;
        result = 31 * result + evictionPolicy.name().hashCode();
        result = 31 * result + maxSizePolicy.name().hashCode();
        result = 31 * result + maxSize;
        return result;
    }
}
