package marvinware.sqlutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public class ImmutableBlob implements Blob {
    private final Blob blob;

    public ImmutableBlob(Blob blob) {
        this.blob = blob;
    }

    @Override
    public long length() throws SQLException {
        return blob.length();
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        return blob.getBytes(pos, length);
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        return blob.getBinaryStream();
    }

    @Override
    public long position(byte[] pattern, long start) throws SQLException {
        return blob.position(pattern, start);
    }

    @Override
    public long position(Blob pattern, long start) throws SQLException {
        return blob.position(pattern, start);
    }

    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify blob");
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify blob");
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify blob");
    }

    @Override
    public void truncate(long len) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify blob");
    }

    @Override
    public void free() throws SQLException {
        throw new SQLFeatureNotSupportedException("Cannot modify blob");
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        return blob.getBinaryStream(pos, length);
    }
}
