package com.marvinware.sqlutils;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;

/**
 * The type Immutable array.
 */
public class ImmutableArray implements Array {
    private final Array array;

    /**
     * Instantiates a new Immutable array.
     *
     * @param array the array
     */
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

    /**
     * @param index the array index of the first element to retrieve;
     *              the first element is at index 1
     * @param count the number of successive SQL array elements to
     *              retrieve
     * @param map   a {@code java.util.Map} object
     *              that contains SQL type names and the classes in
     *              the Java programming language to which they are mapped
     * @return
     * @throws java.sql.SQLException
     */
    @Override
    public Object getArray(long index, int count, java.util.Map<String, Class<?>> map) throws java.sql.SQLException {
        return array.getArray(index, count, map);
    }

    /**
     * @return
     * @throws java.sql.SQLException
     */
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
