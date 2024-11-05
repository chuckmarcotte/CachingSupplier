package marvinware.sqlutils;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;

public class ImmutableArray implements Array {
    private final Array array;

    public ImmutableArray(Array array) {
        this.array = array;
    }

    @Override
    public String getBaseTypeName() throws java.sql.SQLException {
        return array.getBaseTypeName();
    }

    @Override
    public int getBaseType() throws java.sql.SQLException {
        return array.getBaseType();
    }

    @Override
    public Object getArray() throws java.sql.SQLException {
        return array.getArray();
    }

    @Override
    public Object getArray(java.util.Map<String, Class<?>> map) throws java.sql.SQLException {
        return array.getArray(map);
    }

    @Override
    public Object getArray(long index, int count) throws java.sql.SQLException {
        return array.getArray(index, count);
    }

    @Override
    public Object getArray(long index, int count, java.util.Map<String, Class<?>> map) throws java.sql.SQLException {
        return array.getArray(index, count, map);
    }

    @Override
    public ResultSet getResultSet() throws java.sql.SQLException {
        return array.getResultSet();
    }

    @Override
    public ResultSet getResultSet(java.util.Map<String, Class<?>> map) throws java.sql.SQLException {
        return array.getResultSet(map);
    }

    @Override
    public ResultSet getResultSet(long index, int count) throws java.sql.SQLException {
        return array.getResultSet(index, count);
    }

    @Override
    public ResultSet getResultSet(long index, int count, java.util.Map<String, Class<?>> map) throws java.sql.SQLException {
        return array.getResultSet(index, count, map);
    }

    @Override
    public void free() throws java.sql.SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify Array");
    }
}
