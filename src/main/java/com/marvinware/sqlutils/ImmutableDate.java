package com.marvinware.sqlutils;

import java.io.Serializable;

/**
 * The type Immutable date.
 */
public class ImmutableDate extends java.sql.Date implements Serializable {

    /**
     * Instantiates a new Immutable date.
     *
     * @param date the date
     */
    public ImmutableDate(long date) {
        super(date);
    }

    /**
     * Instantiates a new Immutable date.
     *
     * @param date the date
     */
    public ImmutableDate(java.sql.Date date) {
        super(date.getTime());
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
