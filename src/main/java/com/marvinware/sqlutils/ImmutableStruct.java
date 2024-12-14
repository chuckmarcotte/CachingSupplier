package com.marvinware.sqlutils;

import java.sql.Struct;

/**
 * The type Immutable struct.
 */
public class ImmutableStruct implements Struct {
    private final Struct struct;

    /**
     * Instantiates a new Immutable struct.
     *
     * @param struct the struct
     */
    public ImmutableStruct(Struct struct) {
        this.struct = struct;
    }

    @Override
    public String getSQLTypeName() throws java.sql.SQLException {
        return struct.getSQLTypeName();
    }

    @Override
    public Object[] getAttributes() throws java.sql.SQLException {
        return struct.getAttributes();
    }

    @Override
    public Object[] getAttributes(java.util.Map<String, Class<?>> map) throws java.sql.SQLException {
        return struct.getAttributes(map);
    }
}
