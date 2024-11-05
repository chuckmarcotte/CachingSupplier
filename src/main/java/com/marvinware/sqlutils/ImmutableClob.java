package com.marvinware.sqlutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public class ImmutableClob implements Clob {
    private final Clob clob;

    public ImmutableClob(Clob clob) {
        this.clob = clob;
    }

    @Override
    public long length() throws SQLException {
        return clob.length();
    }

    @Override
    public long position(Clob pattern, long start) throws SQLException {
        return clob.position(pattern, start);
    }

    @Override
    public long position(String searchstr, long start) throws SQLException {
        return clob.position(searchstr, start);
    }

    @Override
    public void truncate(long len) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify clob");
    }

    @Override
    public void free() throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify clob");
    }

    @Override
    public String getSubString(long pos, int length) throws SQLException {
        return clob.getSubString(pos, length);
    }

    @Override
    public Reader getCharacterStream() throws SQLException {
        return clob.getCharacterStream();
    }

    @Override
    public InputStream getAsciiStream() throws SQLException {
        return clob.getAsciiStream();
    }

    @Override
    public int setString(long pos, String str) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify clob");
    }

    @Override
    public int setString(long pos, String str, int offset, int len) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify clob");
    }

    @Override
    public OutputStream setAsciiStream(long pos) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify clob");
    }

    @Override
    public Writer setCharacterStream(long pos) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify clob");
    }

    @Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
        return clob.getCharacterStream(pos, length);
    }
}
