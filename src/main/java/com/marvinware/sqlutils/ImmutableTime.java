package com.marvinware.sqlutils;

import java.io.Serializable;

/**
 * The type Immutable time.
 */
public class ImmutableTime extends java.sql.Time implements Serializable {
    /**
     * Instantiates a new Immutable time.
     *
     * @param time the time
     */
    public ImmutableTime(long time) {
        super(time);
    }

    /**
     * Instantiates a new Immutable time.
     *
     * @param time the time
     */
    public ImmutableTime(java.sql.Time time) {
        super(time.getTime());
    }


    @Override
    public void setTime(long time) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
    }

    @Override
    public void setDate(int date) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
    }

    @Override
    public void setHours(int hours) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
    }

    @Override
    public void setMinutes(int minutes) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
    }

    @Override
    public void setMonth(int month) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
    }

    @Override
    public void setSeconds(int seconds) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
    }

    @Override
    public void setYear(int year) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
    }


}
