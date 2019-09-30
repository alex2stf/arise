package com.arise.cargo.impl;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.Context;
import com.arise.core.tools.FileUtil;
import com.arise.cargo.reminiscence.persistence.sql.SQLSyntax;
import java.io.File;
import java.io.IOException;

public class SQLScriptWriter extends Context {

    final SQLSyntax sqlSyntax;

    public SQLScriptWriter(String id, SQLSyntax sqlSyntax) {
        super(id);
        this.sqlSyntax = sqlSyntax;
        this.langTypes = sqlSyntax.getTypesMap();
    }

    @Override
    public void compile() {
        StringBuilder sb = new StringBuilder();
        File createFile = getOutput();
        if (!createFile.exists()){
            try {
                createFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (ARIClazz ARIClazz : getClasses().values()){
            if (ARIClazz.isPersistable() && !ARIClazz.getPersitablePrimitives(this).isEmpty()){
                String block = getClassDef(ARIClazz);
                sb.append(block);
            }
        }
        FileUtil.writeStringToFile(createFile, sb.toString());
    }


    String getClassDef(ARIClazz ARIClazz){
        StringBuilder sb = new StringBuilder();
        String createBlock = sqlSyntax.getCreateTableBlock(ARIClazz);
        sb.append("-- table clazz " + ARIClazz.getName() + "\n");
        sb.append(createBlock).append("\n");
        return sb.toString();
    }



}
