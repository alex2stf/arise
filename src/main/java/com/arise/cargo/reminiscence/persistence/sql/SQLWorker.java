package com.arise.cargo.reminiscence.persistence.sql;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIValue;
import com.arise.cargo.PrimitiveType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.TypeUtil;
import com.arise.core.tools.Util;
import com.arise.cargo.reminiscence.persistence.model.RemoteEntityWorker;
import com.arise.cargo.reminiscence.persistence.sql.SQLSyntax.ColumnInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

public class SQLWorker extends RemoteEntityWorker<ResultSet> {
    private static final Mole log = Mole.getInstance(SQLWorker.class);

        private final Connection connection;
        private final SQLSyntax syntax;
        private DatabaseMetaData databaseMetaData;

        public SQLWorker(Connection connection, SQLSyntax syntax){
            this.connection = connection;
            this.syntax = syntax;
            try {
                this.databaseMetaData = connection.getMetaData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    @Override
    public boolean classExists(ARIClazz table) {
        boolean exists = false;
        ResultSet rs = null;
        String scheman = syntax.getMetadataSchemaName(table);
        String tablen = syntax.getMetadataTableName(table);
        try {
            rs = databaseMetaData.getTables(null, scheman, tablen, null);
            if (rs.next()) {
                exists = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Util.close(rs);

        log.trace("Check if  " + scheman + "." + tablen + " exists: " + exists);
        return exists;
    }


    private boolean runDDLQuery(String query) {
        log.trace(query);
        Statement st = null;
        boolean res = false;
        try {
            st = connection.createStatement();
            res = (st.executeUpdate(query) > 0);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run " + query, e);
        }
        Util.close(st);
        return res;
    }

    @Override
    protected boolean propertyExists(ARIProp column) {
        boolean exists = false;
        ResultSet rs = null;
        try {
            rs = databaseMetaData.getColumns(null,
                syntax.getMetadataSchemaName(column.getParent()),
                syntax.getMetadataTableName(column.getParent()),
                syntax.getMetadataColumnName(column)
            );
            if (rs.next()){
                exists = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Util.close(rs);
        log.trace("ARIProp " + syntax.getColumnName(column) + " exists: " + exists);
        return exists;
    }

    @Override
    protected boolean createClass(ARIClazz table) {
        return runDDLQuery(syntax.getCreateTableStatement(table));
    }

    @Override
    protected void createProperty(ARIProp column) {
        runDDLQuery(syntax.getCreateColumnStatement(column));
    }

    @Override
    protected void dropProperty(ARIProp column) {
        runDDLQuery(syntax.getDropColumnStatement(column));
    }

    @Override
    protected void modifyProperty(ARIProp column) {
        runDDLQuery(syntax.getModifyColumnStatement(column));
    }

    @Override
    public boolean dropClass(ARIClazz table) {
        return runDDLQuery(syntax.getDropTableStatement(table));
    }

    @Override
    protected final boolean propertyMatchExistingSchema(ARIProp column) {
        boolean match = false;
        ResultSet rs = null;
        try {
            rs = databaseMetaData.getColumns(null,
                syntax.getMetadataSchemaName(column.getParent()),
                syntax.getMetadataTableName(column.getParent()),
                syntax.getMetadataColumnName(column)
            );
            if (rs.next()){
                match = compareColumnInfoWithProp(rs, column);
            } else {
                match = false;
            }
            log.trace("Column " + syntax.getColumnName(column) + " match existing definition: " + match);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Util.close(rs);
        return match;
    }


    /**
     * compare database info with currently defined property
     * @param rs
     * @param prop
     * @return
     */
    private boolean compareColumnInfoWithProp(ResultSet rs, ARIProp prop){
        try {
            ColumnInfo info = syntax.fromMetaDataResultSet(rs);

            boolean match1 = info.getTypeName().equalsIgnoreCase(syntax.getSQLTypeName(prop)) &&
                info.isNullable() == prop.isNullable() &&
                info.getName().equalsIgnoreCase(syntax.getColumnName(prop));

            if (match1 && prop.getMaxLength() != null && info.getSize() != null && info.getSize().intValue() > 1){
                return info.getSize().intValue() >= prop.getMaxLength().intValue();
            }
            return match1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




    @Override
    public boolean clazzHasNoProps(ARIClazz table) {
        boolean isEmpty = true;
        ResultSet rs = null;
        try {
            Statement statement = connection.createStatement();
            String head = "s_num_rows";
            String query = syntax.getCountEntriesInTableStatement(table, head);
            log.trace("count: " + query);


            rs = statement.executeQuery(query);
            if (rs.next()){
                Integer count = rs.getInt(head);
                isEmpty = (count != null ? count.intValue() == 0 : false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Util.close(rs);
        return isEmpty;
    }

    @Override
    public void put(ARIClazz table, List<ARIValue> values, Event<Integer> event) {
        String query = syntax.getInsertPreparedStatement(table, values);
        PreparedStatement preparedStatement = null;
        log.trace(query);
        try {

            preparedStatement = connection.prepareStatement(query);
            fillPreparedStatement(preparedStatement, values);
            event.taskComplete(preparedStatement.executeUpdate());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Util.close(preparedStatement);
        }

    }

    @Override
    public void get(ARIClazz table, List<ARIProp> propsToFetch, Event<ResultSet> event, List<ARIValue> clauses) {
        String query = syntax.getSelectPreparedStatement(table, propsToFetch, clauses);
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        log.trace(query);
        try {
            preparedStatement = connection.prepareStatement(query);
            fillPreparedStatement(preparedStatement, clauses);
            rs = preparedStatement.executeQuery();
            event.taskComplete(rs);
        } catch (SQLException e) {
             e.printStackTrace();
        } finally {
            Util.close(preparedStatement);
            Util.close(rs);
        }
    }

//    @Override
//    public void get(ARIClazz table, Event<ResultSet> event, List<ARIValue> values) {
//        String query = syntax.getSelectPreparedStatement(table, values);
//        PreparedStatement preparedStatement = null;
//        ResultSet rs = null;
//        log.trace(query);
//        try {
//            preparedStatement = connection.prepareStatement(query);
//            fillPreparedStatement(preparedStatement, values);
//            rs = preparedStatement.executeQuery();
//            event.taskComplete(rs);
//        } catch (SQLException e) {
//             e.printStackTrace();
//        } finally {
//            Util.close(preparedStatement);
//            Util.close(rs);
//        }
//    }

//    @Override
//    public void get(ARIClazz table, Event<ResultSet> event, ARIValue... props) {
//        String query = syntax.getSelectPreparedStatement(table, props);
//
//        log.trace(query);
//
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//            fillPreparedStatement(preparedStatement, values);
//            event.taskComplete(preparedStatement.executeUpdate());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }


    private void fillPreparedStatement(PreparedStatement preparedStatement, List<ARIValue> vals){
        for (int i = 0; i < vals.size(); i++){
            int index = i+1;
            ARIValue cv = vals.get(i);
            //TODO fix this
            if (cv.prop().isPrimitive(null)){
                PrimitiveType type = PrimitiveType.valueOf(cv.prop().getTypeName().toUpperCase());
                try {
                    fillStatement(preparedStatement, index, type, cv.value());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                log.trace(cv + " is not primitive");
            }
        }
    }


    //TODO improve this:
    private void fillStatement(PreparedStatement statement, int index, PrimitiveType type, Object value) throws SQLException {

        if (TypeUtil.isLong(value)){
            statement.setLong(index, (Long) value);
            return;
        }

        if (value instanceof File){
            try {
                File f = (File) value;
                statement.setCharacterStream(index, new FileReader(f), f.length());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        if (value instanceof InputStream){
            statement.setBlob(index, (InputStream) value);
            return;
        }

        if (value instanceof Blob){
            statement.setBlob(index, (Blob) value);
            return;
        }

        switch (type){
            case BOOL:
                statement.setBoolean(index, (Boolean) value);
                break;
            case INT:
                statement.setInt(index, (Integer) value);
                break;
            case BLOB:
                if (value instanceof InputStream){
                    statement.setBlob(index, (InputStream) value);
                } else {
                    statement.setBlob(index, (Blob) value);
                }
                break;
            case STRING:
                statement.setString(index, String.valueOf(value));
                break;
            case CHAR:
                statement.setString(index, String.valueOf(value));
                break;
            case DATE:
                statement.setDate(index, (Date) value);
                break;
            case DOUBLE:
                statement.setDouble(index, (Double) value);
                break;
            case TIMESTAMP:
                statement.setTimestamp(index, (Timestamp) value);
                break;
        }
    }



    private ARIProp getTableColumn(ARIClazz table, String columnName){
        for (ARIProp prop: table.getProperties()){
            if (columnName.equals(syntax.getColumnName(prop))){
                return prop;
            }
        }
        return null;
    }







}
