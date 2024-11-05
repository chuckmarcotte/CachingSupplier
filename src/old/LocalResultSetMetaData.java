package marvinware.sqlutils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

class LocalResultSetMetaData implements ResultSetMetaData {

    private static class ColumnMetaData {
        String columnTypeName = "";
        String columnClassName = "";
        String catalogName = "";
        String schemaName = "";
        String tableName = "";
        String columnName = "";
        String columnLabel = "";
        int columnType = 0;
        int columnDisplaySize = 0;
        int precision = 0;
        int scale = 0;
        int isNullable = ResultSetMetaData.columnNullable;
        boolean isAuoIncrement = false;
        boolean isCaseSensitive = false;
        boolean isSearchable = false;
        boolean isCurrency = false;
        boolean isSigned = false;
        boolean isReadOnly = false;
        boolean isWritable = false;
        boolean isDefinitelyWritable = false;
    }

    private int colCount = 0;
    private final List<ColumnMetaData> columnMetaDataList = new ArrayList<>();

    public LocalResultSetMetaData(Object[][] tableData, String[] columnLabels) throws SQLException {
        this.colCount = columnLabels.length;
        for (int c=0; c < colCount; c++) {
            ColumnMetaData columnMetaData = new ColumnMetaData();
//            columnMetaData.catalogName = metaData.getCatalogName(c+1);
//            columnMetaData.schemaName = metaData.getSchemaName(c+1);
//            columnMetaData.tableName = metaData.getTableName(c+1);
            columnMetaData.columnName = columnLabels[c];
            columnMetaData.columnLabel = columnLabels[c];
            columnMetaData.columnType = Types.JAVA_OBJECT;
//            columnMetaData.columnDisplaySize = metaData.getColumnDisplaySize(c+1);
//            columnMetaData.precision = metaData.getPrecision(c+1);
//            columnMetaData.scale = metaData.getScale(c+1);
            columnMetaData.isNullable = ResultSetMetaData.columnNullable;
            columnMetaData.isAuoIncrement = false;
            columnMetaData.isCaseSensitive = true;
            columnMetaData.isSearchable = true;
//            columnMetaData.isCurrency = metaData.isCurrency(c+1);
//            columnMetaData.isSigned = metaData.isSigned(c+1);
            columnMetaData.isReadOnly = true;
            columnMetaData.isWritable = false;
            columnMetaData.isDefinitelyWritable = false;
            columnMetaData.columnTypeName = (tableData != null && tableData.length > 0 && tableData[0].length > 0) ? tableData[0][c].getClass().getTypeName() : String.class.getTypeName();
            columnMetaData.columnClassName = (tableData != null && tableData.length > 0 && tableData[0].length > 0) ? tableData[0][c].getClass().getTypeName() : String.class.getTypeName();
            columnMetaDataList.add(columnMetaData);
        }
    }

    public LocalResultSetMetaData(ResultSetMetaData metaData) throws SQLException {
        this.colCount = metaData.getColumnCount();
        for (int c=0; c < colCount; c++) {
            ColumnMetaData columnMetaData = new ColumnMetaData();
            columnMetaData.catalogName = metaData.getCatalogName(c+1);
            columnMetaData.schemaName = metaData.getSchemaName(c+1);
            columnMetaData.tableName = metaData.getTableName(c+1);
            columnMetaData.columnName = metaData.getColumnName(c+1);
            columnMetaData.columnLabel = metaData.getColumnLabel(c+1);
            columnMetaData.columnType = metaData.getColumnType(c+1);
            columnMetaData.columnDisplaySize = metaData.getColumnDisplaySize(c+1);
            columnMetaData.precision = metaData.getPrecision(c+1);
            columnMetaData.scale = metaData.getScale(c+1);
            columnMetaData.isNullable = metaData.isNullable(c+1);
            columnMetaData.isAuoIncrement = metaData.isAutoIncrement(c+1);
            columnMetaData.isCaseSensitive = metaData.isCaseSensitive(c+1);
            columnMetaData.isSearchable = metaData.isSearchable(c+1);
            columnMetaData.isCurrency = metaData.isCurrency(c+1);
            columnMetaData.isSigned = metaData.isSigned(c+1);
            columnMetaData.isReadOnly = metaData.isReadOnly(c+1);
            columnMetaData.isWritable = metaData.isWritable(c+1);
            columnMetaData.isDefinitelyWritable = metaData.isDefinitelyWritable(c+1);
            columnMetaData.columnTypeName = metaData.getColumnTypeName(c+1);
            columnMetaData.columnClassName = metaData.getColumnClassName(c+1);
            columnMetaDataList.add(columnMetaData);
        }
    }

    @Override
    public int getColumnCount() throws SQLException {
        return this.colCount;
    }

    private void checkColRange(int col) throws SQLException {
        if (col <= 0 || col > colCount) {
            throw new SQLException("Invalid column index :"+col);
        }
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).catalogName;
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isAuoIncrement;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isCaseSensitive;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isSearchable;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isCurrency;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isNullable;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isSigned;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).columnDisplaySize;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).columnLabel;
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).columnName;
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).schemaName;
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).precision;
    }

    @Override
    public int getScale(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).scale;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).tableName;
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).columnType;
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).columnTypeName;
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isReadOnly;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isWritable;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).isDefinitelyWritable;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        checkColRange(column);
        return columnMetaDataList.get(column-1).columnClassName;
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
