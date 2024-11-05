package com.marvinware.sqlutils;

import java.sql.Struct;

public class ImmutableStruct implements Struct {
    private final Struct struct;

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
