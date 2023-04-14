package com.teliacompany.tiberius.base.server.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CurrentTimeProvider {
    private static CurrentTimeProvider instance;

    private Clock clock = Clock.system(ZoneId.systemDefault());

    @PostConstruct
    void init() {
        CurrentTimeProvider.instance = this;
    }

    /**
     * Allow static access to the CurrentTimeProvider bean. Prefer Autowire the bean instead but this is useful in static converters etc.
     * Instance will be set as soon as spring has completely initiated the bean and called the init() method. Before that getInstance() will return null.
     */
    public static CurrentTimeProvider getInstance() {
        return instance;
    }

    public LocalDateTime getLocalDateTimeNow() {
        return LocalDateTime.now(clock);
    }

    public LocalDate getLocalDateNow() {
        return LocalDate.now(clock);
    }

    public Instant getInstantNow() {
        return Instant.now(clock);
    }

    public Long getEpochMillisNow() {
        return Instant.now(clock).toEpochMilli();
    }

    public void setClock(long timestamp) {
        this.clock = Clock.fixed(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public void setClock(Instant timestamp) {
        this.clock = Clock.fixed(timestamp, ZoneId.systemDefault());
    }

    public void resetClock() {
        this.clock = Clock.system(ZoneId.systemDefault());
    }

}
