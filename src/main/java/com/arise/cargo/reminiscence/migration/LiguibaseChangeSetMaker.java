package com.arise.cargo.reminiscence.migration;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LiguibaseChangeSetMaker {

    public static void main(String[] args) {

        SQLMigration sqlMigration = new SQLMigration();
        sqlMigration.setFile("/home/alex/Desktop/TMP/tmp2");


        final AtomicInteger atomicInteger = new AtomicInteger(2800);

        final int counter[] = new int[]{500};
        sqlMigration.setStatementHandler(new SQLMigration.StatementHandler() {
            @Override
            public void onStatement(File file, String statement) {
                System.out.println(file.getName());
                File out = new File("TODO/" + file.getName());
                if (!out.exists()){
                    try {
                        out.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(statement);

                Writer output;
                try {
                    output = new BufferedWriter(new FileWriter(out, true));
                    output.append("--changeset mark:" + atomicInteger.incrementAndGet() + "-20181113\n");
                    output.append(statement + ";");
                    output.append("\n\n");
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).execute(null);
    }
}
