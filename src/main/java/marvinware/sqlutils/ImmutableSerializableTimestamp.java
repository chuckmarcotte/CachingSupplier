package marvinware.sqlutils;

import java.io.Serializable;

public class ImmutableSerializableTimestamp extends java.sql.Timestamp implements Serializable {
    public ImmutableSerializableTimestamp(long time) {
        super(time);
    }

    public ImmutableSerializableTimestamp(java.sql.Timestamp timestamp) {
        super(timestamp.getTime());
    }

    @Override
    public void setNanos(int n) {
        throw new RuntimeException("Immutable sql time/date/timestamp");
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
