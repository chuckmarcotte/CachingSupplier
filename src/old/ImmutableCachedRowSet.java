package marvinware.sqlutils;

import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ImmutableCachedRowSet implements CachedRowSet {

    static class ImmutableRowSetException extends RuntimeException {
        public ImmutableRowSetException() {}

        public ImmutableRowSetException(Throwable cause) {
            super(cause);
        }

        public ImmutableRowSetException(String reason) {
            super(reason);
        }

        public ImmutableRowSetException(String reason, Throwable cause) {
            super(reason, cause);
        }
    }

    static class NotImplementedException extends RuntimeException {
        public NotImplementedException() {}

        public NotImplementedException(Throwable cause) {
            super(cause);
        }

        public NotImplementedException(String reason) {
            super(reason);
        }

        public NotImplementedException(String reason, Throwable cause) {
            super(reason, cause);
        }
    }

    private void throwImmutableException() {
        throw new ImmutableRowSetException("RowSet is immutable");
    }

    private void throwNotImplementedException() {
        throw new NotImplementedException("RowSet is immutable and read-only - this feature is not implemented");
    }

    /////////////////////////////////
    // Immutable Exception methods
    /////////////////////////////////

    @Override
    public void acceptChanges() throws SyncProviderException {
        throwImmutableException();
    }

    @Override
    public void setTableName(String tabName) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setKeyColumns(int[] keys) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setShowDeleted(boolean b) throws SQLException {
        throwImmutableException();
    }




    ////////////////////////////
    // Not Implemented methods
    ////////////////////////////

    @Override
    public void execute(Connection conn) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void acceptChanges(Connection con) throws SyncProviderException {
        throwNotImplementedException();
    }

    @Override
    public void restoreOriginal() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void release() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void undoDelete() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void undoInsert() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void undoUpdate() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public SyncProvider getSyncProvider() throws SQLException {
        throwNotImplementedException();
        return null;
    }

    @Override
    public void setSyncProvider(String provider) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void setMetaData(RowSetMetaData md) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public ResultSet getOriginal() throws SQLException {
        throwNotImplementedException();
        return null;
    }

    @Override
    public ResultSet getOriginalRow() throws SQLException {
        throwNotImplementedException();
        return null;
    }

    @Override
    public void setOriginalRow() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public RowSet createShared() throws SQLException {
        throwNotImplementedException();
        return null;
    }

    @Override
    public CachedRowSet createCopy() throws SQLException {
        throwNotImplementedException();
        return null;
    }

    @Override
    public CachedRowSet createCopySchema() throws SQLException {
        throwNotImplementedException();
        return null;
    }

    @Override
    public CachedRowSet createCopyNoConstraints() throws SQLException {
        throwNotImplementedException();
        return null;
    }

    @Override
    public void commit() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void rollback() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void rollback(Savepoint s) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void populate(ResultSet rs, int startRow) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void setPageSize(int size) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public boolean nextPage() throws SQLException {
        throwNotImplementedException();
        return false;
    }

    @Override
    public boolean previousPage() throws SQLException {
        throwNotImplementedException();
        return false;
    }

    @Override
    public void setUrl(String url) throws SQLException {
        throwNotImplementedException();
    }





    ///////////////////////////////////
    // Immutable Decorator methods

    private final CachedRowSet cachedRowSet;

    public ImmutableCachedRowSet(ResultSet data) throws SQLException {
        this.cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
        this.cachedRowSet.populate(data);
    }

    @Override
    public void populate(ResultSet data) throws SQLException {
        cachedRowSet.populate(data);
    }

    @Override
    public boolean columnUpdated(int idx) throws SQLException {
        return false;
    }

    @Override
    public boolean columnUpdated(String columnName) throws SQLException {
        return false;
    }

    @Override
    public Collection<?> toCollection() throws SQLException {
        return cachedRowSet.toCollection();
    }

    @Override
    public Collection<?> toCollection(int column) throws SQLException {
        return cachedRowSet.toCollection(column);
    }

    @Override
    public Collection<?> toCollection(String column) throws SQLException {
        return cachedRowSet.toCollection(column);
    }

    @Override
    public int size() {
        return cachedRowSet.size();
    }


    @Override
    public String getTableName() throws SQLException {
        return cachedRowSet.getTableName();
    }


    @Override
    public int[] getKeyColumns() throws SQLException {
        return cachedRowSet.getKeyColumns();
    }

    @Override
    public RowSetWarning getRowSetWarnings() throws SQLException {
        return cachedRowSet.getRowSetWarnings();
    }

    @Override
    public boolean getShowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void rowSetPopulated(RowSetEvent event, int numRows) throws SQLException {
        cachedRowSet.rowSetPopulated(event, numRows);
    }

    @Override
    public int getPageSize() {
        return cachedRowSet.getPageSize();
    }

    @Override
    public String getUrl() throws SQLException {
        return cachedRowSet.getUrl();
    }

    @Override
    public String getDataSourceName() {
        return cachedRowSet.getDataSourceName();
    }

    @Override
    public void setDataSourceName(String name) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public String getUsername() {
        return cachedRowSet.getUsername();
    }

    @Override
    public void setUsername(String name) throws SQLException {
        throwImmutableException();
    }

    @Override
    public String getPassword() {
        return cachedRowSet.getPassword();
    }

    @Override
    public void setPassword(String password) throws SQLException {
        throwImmutableException();
    }

    @Override
    public int getTransactionIsolation() {
        return cachedRowSet.getTransactionIsolation();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return cachedRowSet.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public String getCommand() {
        return cachedRowSet.getCommand();
    }

    @Override
    public void setCommand(String cmd) throws SQLException {
        throwImmutableException();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setReadOnly(boolean value) throws SQLException {
        throwImmutableException();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return cachedRowSet.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throwImmutableException();
    }

    @Override
    public int getMaxRows() throws SQLException {
        return cachedRowSet.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throwImmutableException();
    }

    @Override
    public boolean getEscapeProcessing() throws SQLException {
        return cachedRowSet.getEscapeProcessing();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throwImmutableException();
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return cachedRowSet.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setType(int type) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setConcurrency(int concurrency) throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setRef(int i, Ref x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBlob(int i, Blob x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setClob(int i, Clob x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setArray(int i, Array x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void clearParameters() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void execute() throws SQLException {
        throwNotImplementedException();
    }

    @Override
    public void addRowSetListener(RowSetListener listener) {
        cachedRowSet.addRowSetListener(listener);
    }

    @Override
    public void removeRowSetListener(RowSetListener listener) {
        cachedRowSet.removeRowSetListener(listener);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throwImmutableException();
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throwImmutableException();
    }

    @Override
    public boolean next() throws SQLException {
        return cachedRowSet.next();
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public boolean wasNull() throws SQLException {
        return cachedRowSet.wasNull();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return cachedRowSet.getString(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return cachedRowSet.getBoolean(columnIndex);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return cachedRowSet.getByte(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return cachedRowSet.getShort(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return cachedRowSet.getInt(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return cachedRowSet.getLong(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return cachedRowSet.getFloat(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return cachedRowSet.getDouble(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return cachedRowSet.getBigDecimal(columnIndex);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return cachedRowSet.getBytes(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return cachedRowSet.getDate(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return cachedRowSet.getTime(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return cachedRowSet.getTimestamp(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return cachedRowSet.getAsciiStream(columnIndex);
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return cachedRowSet.getUnicodeStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return cachedRowSet.getBinaryStream(columnIndex);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return cachedRowSet.getString(columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return cachedRowSet.getBoolean(columnLabel);
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return cachedRowSet.getByte(columnLabel);
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return cachedRowSet.getShort(columnLabel);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return cachedRowSet.getInt(columnLabel);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return cachedRowSet.getLong(columnLabel);
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return cachedRowSet.getFloat(columnLabel);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return cachedRowSet.getDouble(columnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return cachedRowSet.getBigDecimal(columnLabel);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return cachedRowSet.getBytes(columnLabel);
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return cachedRowSet.getDate(columnLabel);
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return cachedRowSet.getTime(columnLabel);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return cachedRowSet.getTimestamp(columnLabel);
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return cachedRowSet.getAsciiStream(columnLabel);
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return cachedRowSet.getUnicodeStream(columnLabel);
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return cachedRowSet.getBinaryStream(columnLabel);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return cachedRowSet.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        throwImmutableException();
    }

    @Override
    public String getCursorName() throws SQLException {
        return cachedRowSet.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return cachedRowSet.getMetaData();
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return cachedRowSet.getObject(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return cachedRowSet.getObject(columnLabel);
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return cachedRowSet.findColumn(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return cachedRowSet.getCharacterStream(columnIndex);
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return cachedRowSet.getCharacterStream(columnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return cachedRowSet.getBigDecimal(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return cachedRowSet.getBigDecimal(columnLabel);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return cachedRowSet.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return cachedRowSet.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return cachedRowSet.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return cachedRowSet.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        cachedRowSet.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        cachedRowSet.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return cachedRowSet.first();
    }

    @Override
    public boolean last() throws SQLException {
        return false;
    }

    @Override
    public int getRow() throws SQLException {
        return 0;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return false;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        return 0;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {

    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
    public void updateRow() throws SQLException {

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {

    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {

    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return "";
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return "";
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public void setMatchColumn(int columnIdx) throws SQLException {

    }

    @Override
    public void setMatchColumn(int[] columnIdxes) throws SQLException {

    }

    @Override
    public void setMatchColumn(String columnName) throws SQLException {

    }

    @Override
    public void setMatchColumn(String[] columnNames) throws SQLException {

    }

    @Override
    public int[] getMatchColumnIndexes() throws SQLException {
        return new int[0];
    }

    @Override
    public String[] getMatchColumnNames() throws SQLException {
        return new String[0];
    }

    @Override
    public void unsetMatchColumn(int columnIdx) throws SQLException {

    }

    @Override
    public void unsetMatchColumn(int[] columnIdxes) throws SQLException {

    }

    @Override
    public void unsetMatchColumn(String columnName) throws SQLException {

    }

    @Override
    public void unsetMatchColumn(String[] columnName) throws SQLException {

    }
}
