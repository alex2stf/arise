package com.arise.cargo.reminiscence.persistence.sql.syntaxes;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.core.tools.BlockBuilder;
import com.arise.cargo.PrimitiveType;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.StringUtil.JoinIterator;
import com.arise.cargo.reminiscence.persistence.sql.SQLSyntax;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ORASyntax extends SQLSyntax {

    private static final Map<PrimitiveType, ARIType> oracleTypes = new HashMap<>();

    static {
        oracleTypes.put(PrimitiveType.CHAR, ARIType.simple("CHAR"));
        oracleTypes.put(PrimitiveType.STRING, ARIType.simple("VARCHAR"));
        oracleTypes.put(PrimitiveType.STRING, ARIType.simple("VARCHAR2"));
        oracleTypes.put(PrimitiveType.LONG, ARIType.simple("NUMBER"));
        oracleTypes.put(PrimitiveType.INT, ARIType.simple("NUMBER"));
        oracleTypes.put(PrimitiveType.DATE, ARIType.simple("DATE"));
        oracleTypes.put(PrimitiveType.DOUBLE, ARIType.simple("NUMBER"));
        oracleTypes.put(PrimitiveType.BOOL, ARIType.simple("NUMBER"));
        oracleTypes.put(PrimitiveType.FILE, ARIType.simple("CLOB"));
        oracleTypes.put(PrimitiveType.BLOB, ARIType.simple("CLOB"));
        oracleTypes.put(PrimitiveType.TIMESTAMP, ARIType.simple("TIMESTAMP"));
    }




    @Override
    public String getFullyQualifiedTableName(ARIClazz table) {
        return (table.getSchema() != null ? (table.getSchema() + "." + getTableName(table)) : getTableName(table)).toLowerCase();
    }



    @Override
    public String getCreateTableBlock(ARIClazz clazz) {
        BlockBuilder blockBuilder = new BlockBuilder();
        blockBuilder.writeLine("BEGIN")
        .writeLine("EXECUTE IMMEDIATE 'CREATE TABLE ", getFullyQualifiedTableName(clazz), "(");

        //TODO fix this
        blockBuilder.getBlock("create-syntax").join(clazz.getPersitablePrimitives(null), ",", new JoinIterator<ARIProp>() {
            @Override
            public String toString(ARIProp value) {
                return getColumnDefinition(value);
            }
        });

        blockBuilder.writeLine(" )';").writeLine("EXCEPTION");

        blockBuilder.getBlock("ex")
            .writeLine("WHEN OTHERS THEN")
            .writeLine("IF SQLCODE = -955 THEN NULL; -- suppresses ORA-00955 exception")
            .writeLine("ELSE RAISE;")
            .writeLine("END IF;")
            .writeLine("END; ")
        ;

        blockBuilder.writeLine("/");
        return blockBuilder.toString();
    }

    @Override
    public Map<PrimitiveType, ARIType> getTypes() {
        return oracleTypes;
    }

    @Override
    public String getMetadataSchemaName(ARIClazz table) {
       if (table.getSchema() != null){
           return table.getSchema().toUpperCase();
       }
       return null;
    }

    @Override
    public String getMetadataTableName(ARIClazz table) {
        return getTableName(table).toUpperCase();
    }

    @Override
    public String getCreateTableStatement(ARIClazz table) {
        return "CREATE TABLE " + getFullyQualifiedTableName(table) + "(" + StringUtil.join(table.getProperties(), ",", new JoinIterator<ARIProp>() {
                @Override
                public String toString(ARIProp value) {
                    return getColumnDefinition(value);
                }
        }) + ")";
    }

    @Override
    public String getMetadataColumnName(ARIProp column) {
        return getColumnName(column).toUpperCase();
    }

    @Override
    public String getDropTableStatement(ARIClazz table) {
        return "DROP TABLE " + getFullyQualifiedTableName(table) + " CASCADE CONSTRAINTS";
    }

    public String getColumnDefinition(ARIProp prop){
        String sqlType = this.getSQLTypeName(prop);
        return prop.getName() + " " + getColumnArgs(prop, sqlType);
    }

    public String getColumnArgs(ARIProp prop, String sqlType){
        String response = sqlType;
        if (prop.getMaxLength() != null){
            response += "(" + prop.getMaxLength() + ")";
        } else if ("VARCHAR".equalsIgnoreCase(sqlType)){
            response += "(255)";
        }
        if (!prop.isNullable()){
            response += " NOT NULL";
        }
        if (prop.getDefaultValue() != null) {
            response += " DEFAULT " + prop.getDefaultValue();
        }
        return response;
    }




    @Override
    public ColumnInfo fromMetaDataResultSet(ResultSet resultSet) throws Exception {
        Integer size = resultSet.getInt("COLUMN_SIZE");
        String name = resultSet.getString("COLUMN_NAME");
        boolean isNullable = "YES".equalsIgnoreCase(resultSet.getString("IS_NULLABLE"));
        String typeName = resultSet.getString("TYPE_NAME");
        return new ColumnInfo(size, name, isNullable, typeName);
    }

    @Override
    public String getModifyColumnStatement(ARIProp column) {
        return "ALTER TABLE " + getFullyQualifiedTableName(column.getParent()) + " MODIFY " + getColumnName(column) + " " + getColumnArgs(column, getSQLTypeName(column));
    }

    @Override
    public String getDropColumnStatement(ARIProp column) {
        return "ALTER TABLE "+getFullyQualifiedTableName(column.getParent())+" DROP COLUMN " + getColumnName(column);
    }

    @Override
    public String getCreateColumnStatement(ARIProp column) {
        return "ALTER TABLE " + getFullyQualifiedTableName(column.getTable()) + " ADD " + getColumnDefinition(column);
    }


}
