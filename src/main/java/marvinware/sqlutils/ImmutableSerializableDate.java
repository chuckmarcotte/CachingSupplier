package marvinware.sqlutils;

import java.io.Serializable;

public class ImmutableSerializableDate extends java.sql.Date implements Serializable {

    public ImmutableSerializableDate(long date) {
        super(date);
    }

    public ImmutableSerializableDate(java.sql.Date date) {
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
