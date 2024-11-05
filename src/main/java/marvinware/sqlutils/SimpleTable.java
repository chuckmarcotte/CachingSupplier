package marvinware.sqlutils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTable implements Serializable, Cloneable {
    private Object[][] table;
    private final String[] columnHeaders;
    private final int[] columnTypes;

    private final Map<Integer, String> colIndexToName = new HashMap<>();
    private final Map<String, Integer> colNameToIndex = new HashMap<>();

    public SimpleTable(ResultSet rset) throws SQLException {
        ResultSetMetaData resultSetMetaData = rset.getMetaData();
        int colCnt = resultSetMetaData.getColumnCount();
        table = new Object[colCnt][];
        columnHeaders = new String[colCnt];
        columnTypes = new int[colCnt];

        for (int c=0; c < colCnt; c++) {
            columnHeaders[c] = resultSetMetaData.getColumnLabel(c);
            columnTypes[c] = resultSetMetaData.getColumnType(c);
            colIndexToName.put(c+1, columnHeaders[c]);
            colNameToIndex.put(columnHeaders[c], c+1);
        }

        List<Object> colVals = new ArrayList<>();
        for (int c=0; c < colCnt; c++ ) {
            while (rset.next()) {
                Object val = rset.getObject(c+1);
                if (val instanceof Date) {
                    val = new ImmutableSerializableDate((java.sql.Date) val);
                }
                if (val instanceof Time) {
                    val = new ImmutableSerializableTime((java.sql.Time) val);
                }
                if (val instanceof Timestamp) {
                    val = new ImmutableSerializableTimestamp((java.sql.Timestamp) val);
                }
                colVals.add(rset.wasNull() ? null : val);
            }
            table[c] = colVals.toArray();
            colVals.clear();
        }
    }

    @Override
    protected Object clone() {
        return deepClone(this);
    }

    public static Object deepClone(Object object) {
        try {
            ByteArrayOutputStream baOs = new ByteArrayOutputStream();
            ObjectOutputStream oOs = new ObjectOutputStream(baOs);
            oOs.writeObject(object);
            ByteArrayInputStream baIs = new ByteArrayInputStream(baOs.toByteArray());
            ObjectInputStream oIs = new ObjectInputStream(baIs);
            return oIs.readObject();
        }
        catch (Exception e) {
            throw new RuntimeException("deepClone() failed: ", e);
        }
    }

    public int getColumnCount() {
        return table == null ? 0 : table.length;
    }

    public int getRowCount() {
        return table == null || table[0] == null ? 0 : table[0].length;
    }

    private void checkIndexes(int columnIndex, int rowIndex) {
        if (table == null || columnIndex > getColumnCount() || columnIndex < 1) {
            throw new RuntimeException("Column index not in the range 1 to " + getColumnCount() + " (inclusive)");
        }
        if (table == null || rowIndex > getRowCount() || rowIndex < 1) {
            throw new RuntimeException("Row index not in the range 1 to " + getRowCount() + " (inclusive)");
        }
    }

    public String getString(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        return value == null ? null : value.toString();
    }

    public boolean getBoolean(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        try {
            return Double.compare(Double.parseDouble(value.toString()), 0) != 0;
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid boolean value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
    }


    public byte getByte(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Byte) {
            return (Byte) value;
        }
        try {
            return (Byte.parseByte(value.toString()));
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid byte value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
    }


    public short getShort(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Short) {
            return (Short) value;
        }
        try {
            return (Short.parseShort(value.toString()));
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid short value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
    }


    public int getInt(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            return (Integer.parseInt(value.toString()));
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid int value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
    }


    public long getLong(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        try {
            return (Long.parseLong(value.toString()));
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid long value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
    }


    public float getFloat(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        try {
            return (Float.parseFloat(value.toString()));
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid float value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
    }

    public double getDouble(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        try {
            return (Double.parseDouble(value.toString()));
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid double value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
    }

    public BigDecimal getBigDecimal(int columnIndex, int rowIndex, int scale) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        BigDecimal ret;

        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            ret = (BigDecimal) value;
        }
        try {
            ret = new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid BigDecimal value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
        ret = ret.setScale(scale);
        return ret;
    }

    private boolean isBinary(int type) {
        return switch (type) {
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> true;
            default -> false;
        };
    }

    public byte[] getBytes(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        BigDecimal ret;

        if (!isBinary(columnTypes[columnIndex])) {
            throw new SQLException(String.format("Column (%s) at index (%d) is not a binary column", colIndexToName.get(columnIndex), columnIndex));
        }

        return (byte[])(table[columnIndex-1][rowIndex-1]);
    }

    public Date getDate(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return null;
        }
        switch (columnTypes[columnIndex-1]) {
            case java.sql.Types.DATE: {
                long sec = ((java.sql.Date)value).getTime();
                return new java.sql.Date(sec);
            }
            case java.sql.Types.TIMESTAMP: {
                long sec = ((java.sql.Timestamp)value).getTime();
                return new java.sql.Date(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat df = DateFormat.getDateInstance();
                    return ((java.sql.Date)(df.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException("Invalid date format (%s) for column (%d)", value.toString().trim(), columnIndex);
                }
            }
            default: {
                throw new SQLException("Cannot convert to a date (%s) for column (%d)", value.toString().trim(), columnIndex);
            }
        }
    }

    public Time getTime(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return null;
        }
        switch (columnTypes[columnIndex-1]) {
            case java.sql.Types.TIME: {
                return (java.sql.Time)value;
            }
            case java.sql.Types.TIMESTAMP: {
                long sec = ((java.sql.Timestamp)value).getTime();
                return new java.sql.Time(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat tf = DateFormat.getTimeInstance();
                    return ((java.sql.Time)(tf.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException("Invalid time format (%s) for column (%d)", value.toString().trim(), columnIndex);
                }
            }
            default: {
                throw new SQLException("Cannot convert to a date (%s) for column (%d)", value.toString().trim(), columnIndex);
            }
        }
    }

    public Timestamp getTimestamp(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        if (value == null) {
            return null;
        }

        switch (columnTypes[columnIndex-1]) {
            case java.sql.Types.TIMESTAMP: {
                return (java.sql.Timestamp) value;
            }
            case java.sql.Types.TIME: {
                long sec = ((java.sql.Time) value).getTime();
                return new java.sql.Timestamp(sec);
            }
            case java.sql.Types.DATE: {
                long sec = ((java.sql.Date) value).getTime();
                return new java.sql.Timestamp(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat tf = DateFormat.getTimeInstance();
                    return ((java.sql.Timestamp) (tf.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException("Invalid timestamp format (%s) for column (%d)", value.toString().trim(), columnIndex);
                }
            }
            default: {
                throw new SQLException("Cannot convert to a timestamp (%s) for column (%d)", value.toString().trim(), columnIndex);
            }
        }
    }

    public String getString(String columnLabel, int rowIndex) throws SQLException {
        return getString((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public boolean getBoolean(String columnLabel, int rowIndex) throws SQLException {
        return getBoolean((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public byte getByte(String columnLabel, int rowIndex) throws SQLException {
        return getByte((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public short getShort(String columnLabel, int rowIndex) throws SQLException {
        return getShort((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public int getInt(String columnLabel, int rowIndex) throws SQLException {
        return getInt((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public long getLong(String columnLabel, int rowIndex) throws SQLException {
        return getLong((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public float getFloat(String columnLabel, int rowIndex) throws SQLException {
        return getFloat((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public double getDouble(String columnLabel, int rowIndex) throws SQLException {
        return getDouble((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public BigDecimal getBigDecimal(String columnLabel, int rowIndex, int scale) throws SQLException {
        return getBigDecimal((int) colNameToIndex.get(columnLabel), rowIndex, scale);
    }


    public byte[] getBytes(String columnLabel, int rowIndex) throws SQLException {
        return getBytes((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public Date getDate(String columnLabel, int rowIndex) throws SQLException {
        return getDate((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public Time getTime(String columnLabel, int rowIndex) throws SQLException {
        return getTime((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public Timestamp getTimestamp(String columnLabel, int rowIndex) throws SQLException {
        return getTimestamp((int) colNameToIndex.get(columnLabel), rowIndex);
    }


    public Object getObject(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        return table[columnIndex-1][rowIndex-1];
    }

    public Object getObject(String columnLabel, int rowIndex) throws SQLException {
        int ci = colNameToIndex.get(columnLabel);
        checkIndexes(ci, rowIndex);
        return table[ci][rowIndex-1];
    }

    public int findColumn(String columnLabel) throws SQLException {
        return colNameToIndex.get(columnLabel);
    }

    public BigDecimal getBigDecimal(int columnIndex, int rowIndex) throws SQLException {
        checkIndexes(columnIndex, rowIndex);
        Object value = table[columnIndex-1][rowIndex-1];
        BigDecimal ret;

        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            ret = (BigDecimal) value;
        }
        try {
            ret = new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            throw new SQLException(String.format("Invalid BigDecimal value (%s) for column index (%d)", value.toString().trim(), columnIndex));
        }
        return ret;
    }


    public BigDecimal getBigDecimal(String columnLabel, int rowIndex) throws SQLException {
        return getBigDecimal(colNameToIndex.get(columnLabel), rowIndex);
    }

}
