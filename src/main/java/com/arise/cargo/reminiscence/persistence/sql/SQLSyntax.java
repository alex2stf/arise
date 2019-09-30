package com.arise.cargo.reminiscence.persistence.sql;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIValue;
import com.arise.cargo.ARIType;
import com.arise.cargo.PrimitiveType;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.StringUtil.JoinIterator;
import com.arise.cargo.reminiscence.persistence.sql.impl.WhereJoiner;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SQLSyntax {



    /**
     * this should return the table name including the schema name
     * @param table
     * @return
     */
    public abstract String getFullyQualifiedTableName(ARIClazz table);

    public String getTableName(ARIClazz table){
        return (table.getAlias() != null ? table.getAlias() : table.getName()).toLowerCase();
    }

    public String getColumnName(ARIProp prop){
        return (prop.getAlias() != null ? prop.getAlias() : prop.getName()).toLowerCase();
    }

    public abstract String getCreateTableBlock(ARIClazz table);

    public abstract Map<PrimitiveType, ARIType> getTypes();

    public final Map<String, ARIType> getTypesMap(){
        Map<String, ARIType> r = new HashMap<>();
        for (Map.Entry<PrimitiveType, ARIType> entry: this.getTypes().entrySet()){
            r.put(entry.getKey().name().toLowerCase(), entry.getValue());
        }
        return r;
    }

    public String getSQLTypeName(ARIProp prop){
         PrimitiveType t = PrimitiveType.valueOf(prop.getTypeName().toUpperCase());
         return getTypes().get(t).getName();
    }

    public abstract String getMetadataSchemaName(ARIClazz table);

    public abstract String getMetadataTableName(ARIClazz table);

    public abstract String getCreateTableStatement(ARIClazz table);

    public abstract String getMetadataColumnName(ARIProp column);

    public abstract String getDropTableStatement(ARIClazz table);

    public abstract  ColumnInfo fromMetaDataResultSet(ResultSet resultSet) throws Exception;

    public abstract String getModifyColumnStatement(ARIProp column);

    public String getCountEntriesInTableStatement(ARIClazz table, String head) {
        return "SELECT count(*) " + head + " FROM " + getFullyQualifiedTableName(table);
    }

    public abstract String getDropColumnStatement(ARIProp column);

    public abstract String getCreateColumnStatement(ARIProp column);





    public String getInsertPreparedStatement(ARIClazz table, List<ARIValue> values){

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ")
            .append(getFullyQualifiedTableName(table));

        String columns = StringUtil.join(values, ",", new JoinIterator<ARIValue>() {
            @Override
            public String toString(ARIValue x) {
                return getColumnName(x.prop());
            }
        });

        String vls = StringUtil.join(values, ",", new JoinIterator<ARIValue>() {
            @Override
            public String toString(ARIValue value) {
                return "?";
            }
        });

        sb.append("(").append(columns).append(") VALUES (").append(vls).append(")");
        return sb.toString();
    }

    public String getSelectPreparedStatement(ARIClazz table, List<ARIProp> propsToFetch, List<ARIValue> clauses) {
        String alias = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (propsToFetch.isEmpty()){
            sb.append("*");
        } else {
            sb.append(StringUtil.join(propsToFetch, ",", new JoinIterator<ARIProp>() {
                @Override
                public String toString(ARIProp value) {
                    return getColumnName(value);
                }
            }));
        }

        sb.append(" FROM ").append(getFullyQualifiedTableName(table));


        String where = getWhereClauses(clauses, alias);
        if (StringUtil.hasContent(where)){
            sb.append(" ").append(where);
        }
        return sb.toString();
    }


    protected String getWhereClauses(List<ARIValue> clauses, String alias){
        if (clauses.isEmpty()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");
        String s = StringUtil.join(clauses, " AND ", new WhereJoiner(this, alias));
        sb.append(s);
        return sb.toString();
    }















    public static class ColumnInfo {

        public Integer getSize() {
            return size;
        }

        public String getName() {
            return name;
        }

        public boolean isNullable() {
            return isNullable;
        }

        public String getTypeName() {
            return typeName;
        }

        private final Integer size;
        private final String name;
        private final boolean isNullable;
        private final String typeName;

        public ColumnInfo(Integer size, String name, boolean isNullable, String typeName) {
            this.size = size;
            this.name = name;
            this.isNullable = isNullable;
            this.typeName = typeName;
        }
    }
}
