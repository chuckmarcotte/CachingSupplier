package marvinware.sqlutils;

import javax.sql.rowset.serial.SQLInputImpl;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.*;

//import static sun.reflect.misc.ReflectUtil.*;

public class LocalResultSet implements ResultSet {
    int rowCount = 0;
    int colCount = 0;
    int rowIndex0 = -1;
    int columnIndex0 = -1;
    Object[][] tableData = null;
    ResultSetMetaData metaData = null;
    final Map<String, Integer> columnLabelToIndexMap = new HashMap<>();

    public LocalResultSet(Object[][] tableData, String[] columnLabels) throws SQLException {
        this.tableData = tableData;
        rowCount = tableData.length;
        colCount = tableData[0] != null ? tableData[0].length : 0;
        metaData = new LocalResultSetMetaData(tableData, columnLabels);
        for (int c=0; c < columnLabels.length; c++) {
            columnLabelToIndexMap.put(columnLabels[c], c);
        }
    }

    public LocalResultSet(ResultSet containedResultSet) {
        try {
            this.metaData = new LocalResultSetMetaData(containedResultSet.getMetaData());
            if (!containedResultSet.isBeforeFirst()) {
                containedResultSet.beforeFirst();
            }
            colCount = containedResultSet.getMetaData().getColumnCount();
            List<Object[]> rows = new ArrayList<Object[]>();

            while (containedResultSet.next()) {
                rowCount++;
                Object[] rowData = new Object[colCount];
                for (int c = 0; c < colCount; c++) {
                    rowData[c] = containedResultSet.getObject(c + 1);
                    columnLabelToIndexMap.put(containedResultSet.getMetaData().getColumnLabel(c + 1), c);
                }
                rows.add(rowData);
            }
            tableData = new Object[rowCount][colCount];
            for (int r=0; r < rowCount; r++) {
                tableData[r] = rows.get(r);
            }
            rows = null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIndex(int col) throws SQLException {
        if (col <= 0 || col > colCount) {
            throw new SQLException("Invalid column index :" + col);
        }
    }

    private void checkColLabel(String colLabel) throws SQLException {
        if (!columnLabelToIndexMap.containsKey(colLabel)) {
            throw new SQLException("Unknown column label :" + colLabel);
        }
    }

    private void checkCursor() throws SQLException {
        if (isAfterLast() || isBeforeFirst()) {
            throw new SQLException("Invalid cursor location");
        }
    }

    private boolean isBinary(int type) {
        switch (type) {
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }



    @Override
    public boolean next() throws SQLException {
        rowIndex0++;
        return rowIndex0 >= rowCount;
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public boolean wasNull() throws SQLException {
        return tableData[rowIndex0][columnIndex0] == null;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        return tableData[rowIndex0][columnIndex0] == null ? null : tableData[rowIndex0][columnIndex0].toString();
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];
         
        // check for SQL NULL
        if (value == null) {
            return false;
        }

        // check for Boolean...
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        if (value instanceof String && "true".equalsIgnoreCase((String)value)) {
            return Boolean.TRUE;
        }

        // convert to a Double and compare to zero
        try {
            return Double.compare(Double.parseDouble(value.toString()), 0) != 0;
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format("Not a boolean or boolean string or parseable number: val={0} (row={1}, column={2})",
                    value.toString().trim(), this.rowIndex0, this.columnIndex0));
        }
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];

        // check for SQL NULL
        if (value == null) {
            return 0;
        }

        // check for Byte...
        if (value instanceof Byte) {
            return (Byte) value;
        }

        // convert to a Byte
        try {
            return Byte.parseByte(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format("Not a valid Byte or byte string or parseable number: val={0} (row={1}, column={2})",
                    value.toString().trim(), this.rowIndex0, this.columnIndex0));
        }
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];

        // check for SQL NULL
        if (value == null) {
            return 0;
        }

        // check for Short...
        if (value instanceof Short) {
            return (Short) value;
        }

        // convert to a Short
        try {
            return Short.parseShort(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format("Not a valid Short or short string or parseable number: val={0} (row={1}, column={2})",
                    value.toString().trim(), this.rowIndex0, this.columnIndex0));
        }
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];

        // check for SQL NULL
        if (value == null) {
            return 0;
        }

        // check for Int...
        if (value instanceof Integer) {
            return (Integer) value;
        }

        // convert to a Integer
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format("Not a valid Integer or integer string or parseable number: val={0} (row={1}, column={2})",
                    value.toString().trim(), this.rowIndex0, this.columnIndex0));
        }
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];

        // check for SQL NULL
        if (value == null) {
            return 0;
        }

        // check for Long...
        if (value instanceof Long) {
            return (Long) value;
        }

        // convert to a Long
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format("Not a valid Long or long string or parseable number: val={0} (row={1}, column={2})",
                    value.toString().trim(), this.rowIndex0, this.columnIndex0));
        }
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];

        // check for SQL NULL
        if (value == null) {
            return 0;
        }

        // check for Float...
        if (value instanceof Float) {
            return (Float) value;
        }

        // convert to a Float
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format("Not a valid Float or float string or parseable number: val={0} (row={1}, column={2})",
                    value.toString().trim(), this.rowIndex0, this.columnIndex0));
        }
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];

        // check for SQL NULL
        if (value == null) {
            return 0;
        }

        // check for Double...
        if (value instanceof Double) {
            return (Double) value;
        }

        // convert to a Double
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format("Not a valid Double or double string or parseable number: val={0} (row={1}, column={2})",
                    value.toString().trim(), this.rowIndex0, this.columnIndex0));
        }
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        checkIndex(columnIndex);
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];
        BigDecimal bDecimal, retVal;

        // check for SQL NULL
        if (value == null) {
            return (new BigDecimal(0));
        }

        bDecimal = this.getBigDecimal(columnIndex);
        retVal = bDecimal.setScale(scale);

        return retVal;
    }

    // TODO
    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        this.columnIndex0 = columnIndex - 1;
        Object value = tableData[this.rowIndex0][this.columnIndex0];

        checkIndex(columnIndex);
        checkCursor();

        if (!isBinary(metaData.getColumnType(columnIndex))) {
            throw new SQLException("Column is not a binary type: " + columnIndex);
        }

        return (byte[]) value;
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return getString(columnLabelToIndexMap.get(columnLabel));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return false;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return 0;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return 0;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return 0;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return 0;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return 0;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return new byte[0];
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        return "";
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return metaData;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return columnLabelToIndexMap.get(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        checkIndex(columnIndex);
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.rowIndex0 == -1;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return this.rowIndex0 == this.rowCount;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return this.rowIndex0 == 0;
    }

    @Override
    public boolean isLast() throws SQLException {
        return this.rowIndex0 == this.rowCount - 1;
    }

    @Override
    public void beforeFirst() throws SQLException {
        this.rowIndex0 = -1;
    }

    @Override
    public void afterLast() throws SQLException {
        this.rowIndex0 = this.rowCount;
    }

    @Override
    public boolean first() throws SQLException {
        this.rowIndex0 = -1;
        return this.rowCount > 0;
    }

    @Override
    public boolean last() throws SQLException {
        this.rowIndex0 = rowCount - 1;
        return this.rowCount > 0;
    }

    @Override
    public int getRow() throws SQLException {
        return this.rowIndex0 + 1;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        if (row > 0) { // we are moving foward
            if (row > rowCount) {
                // fell off the end
                afterLast();
                return false;
            } else {
                rowIndex0 = row - 1;
            }
        } else { // we are moving backward
            if (rowCount + row < 0) {
                // fell off the front
                beforeFirst();
                return false;
            } else {
                rowIndex0 = rowCount + row;
            }
        }

        return !isAfterLast() && !isBeforeFirst();
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        if (rows > 0) { // we are moving foward
            if (rowIndex0 + rows > rowCount) {
                // fell off the end
                afterLast();
                return false;
            } else {
                rowIndex0 += rows - 1;
            }
        } else { // we are moving backward
            if (rowIndex0 + rows < 0) {
                // fell off the front
                beforeFirst();
                return false;
            } else {
                rowIndex0 = rowIndex0 + rows;
            }
        }

        return !isAfterLast() && !isBeforeFirst();
    }

    @Override
    public boolean previous() throws SQLException {
        return --rowIndex0 < 0;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException("Fetching direction is not supported.");
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_UNKNOWN;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException("Fetching size is not supported.");
    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
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
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void insertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void refreshRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        Object value;
        value = tableData[rowIndex0][columnIndex - 1];

        // check for SQL NULL
        if (value == null) {
            return null;
        }
        if (value instanceof Struct) {
            Struct s = (Struct)value;

            // look up the class in the map
            Class<?> c = map.get(s.getSQLTypeName());
            if (c != null) {
                // create new instance of the class
                SQLData obj = null;
                try {
//                    checkPackageAccess(c);
                    @SuppressWarnings("deprecation")
                    Object tmp = c.newInstance();
                    obj = (SQLData) tmp;
                } catch(Exception ex) {
                    throw new SQLException("Unable to Instantiate: ", ex);
                }
                // get the attributes from the struct
                Object attribs[] = s.getAttributes(map);
                // create the SQLInput "stream"
                SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                // read the values...
                obj.readSQL(sqlInput, s.getSQLTypeName());
                return (Object)obj;
            }
        }
        return value;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getRef() on an Immutable ResultSet is not supported.");
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        Object value = tableData[rowIndex0][columnIndex - 1];

        if (getMetaData().getColumnType(columnIndex) != java.sql.Types.BLOB) {
            throw new SQLException("Wrong column type for getBlob(): " + getMetaData().getColumnType(columnIndex));
        }

        // check for SQL NULL
        if (value == null) {
            return null;
        }

        return (Blob) value;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        Object value = tableData[rowIndex0][columnIndex - 1];

        if (getMetaData().getColumnType(columnIndex) != java.sql.Types.CLOB) {
            throw new SQLException("Wrong column type for getClob(): " + getMetaData().getColumnType(columnIndex));
        }

        // check for SQL NULL
        if (value == null) {
            return null;
        }

        return (Clob) value;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        Object value = tableData[rowIndex0][columnIndex - 1];

        if (getMetaData().getColumnType(columnIndex) != Types.ARRAY) {
            throw new SQLException("Wrong column type for getArray: " + getMetaData().getColumnType(columnIndex));
        }

        // check for SQL NULL
        if (value == null) {
            return null;
        }

        return (java.sql.Array) value;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getRef() on an Immutable ResultSet is not supported.");
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return getRef(columnLabelToIndexMap.get(columnLabel));
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
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
        checkColLabel(columnLabel);
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
        checkColLabel(columnLabel);

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        checkColLabel(columnLabel);

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
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
        checkColLabel(columnLabel);
        return "";
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        checkColLabel(columnLabel);
        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Updating an Immutable ResultSet is not possible.");
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        checkColLabel(columnLabel);
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
}

