package com.arise.cargo.reminiscence.persistence.sql.impl;

import com.arise.cargo.ARIValue;
import com.arise.core.tools.StringUtil;
import com.arise.cargo.reminiscence.persistence.sql.SQLSyntax;

public class WhereJoiner implements StringUtil.JoinIterator<ARIValue> {

    private final SQLSyntax sqlSyntax;
    private final String alias;

    public WhereJoiner(SQLSyntax sqlSyntax, String alias){
        this.sqlSyntax = sqlSyntax;
        this.alias = alias != null ? alias : "";
    }

    @Override
    public String toString(ARIValue value) {
        switch (value.scope()){
            case IN:
                return getC(value) + " IN (" + StringUtil.join(value.argsSize(), ",", "?") + ")";
            case LIKE:
                return getC(value) + " LIKE ?";
            case EQUALS:
                return getC(value) + " = ?";
        }
        return null;
    }

    private String getC(ARIValue val){
        if (StringUtil.hasContent(alias)){
            return alias + "." + sqlSyntax.getColumnName(val.prop());
        }
        return sqlSyntax.getColumnName(val.prop());
    }
}
