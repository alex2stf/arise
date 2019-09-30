package com.arise.cargo.reminiscence.migration;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIValue;
import com.arise.cargo.reminiscence.persistence.model.RemoteEntityWorker;
import com.arise.cargo.reminiscence.persistence.model.RemoteEntityWorker.Event;
import com.arise.cargo.reminiscence.persistence.sql.SQLSyntax;
import com.arise.cargo.reminiscence.persistence.sql.syntaxes.ORASyntax;
import com.arise.cargo.PrimitiveType;
import com.arise.cargo.reminiscence.persistence.sql.SQLWorker;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SQLMigrationTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException {

        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection connection = null;

        //jdbc:oracle:thin:@localhost:1521:SID
        connection = DriverManager.getConnection("jdbc:oracle:thin:@10.200.0.113:1521:XE","dh_loan","oracle");
        SQLSyntax sqlSyntax = new ORASyntax();

//        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM loans");
//
//        ResultSet rs = preparedStatement.executeQuery();
//
//        while (rs.next()){
//            System.out.println(rs.getInt("id") + rs.getString("code"));
//        }
//
//        connection.close();

        RemoteEntityWorker worker = new SQLWorker(connection, sqlSyntax);
        ARIClazz table = ARIClazz.newTable("dh_loan", "test_tbl_3");

        ARIProp tString = table.addColumn("t_string", PrimitiveType.STRING, false, true, true, 100);
        ARIProp tInt = table.addColumn("t_int", PrimitiveType.INT, false, false, false);
        ARIProp tDbl = table.addColumn("t_dbl", PrimitiveType.DOUBLE, false, false, false);
        ARIProp tLong = table.addColumn("t_long", PrimitiveType.LONG, false, false, false);

        ARIProp tChar = table.addColumn("t_char", PrimitiveType.CHAR, false, false, false);
        ARIProp tBool = table.addColumn("t_bool", PrimitiveType.BOOL, false, false, false);
        ARIProp tDate = table.addColumn("t_date", PrimitiveType.DATE, false, false, false);
        ARIProp tTime = table.addColumn("t_time", PrimitiveType.TIMESTAMP, false, false, false);
        ARIProp tFile = table.addColumn("t_file", PrimitiveType.FILE, false, false, false);
        ARIProp tBlob = table.addColumn("t_blob", PrimitiveType.BLOB, false, false, false);



        //operations:
//        if (worker.classExists(table)) {
//            worker.dropClass(table);
//        }
        worker.createClassIfNotExists(table);
        worker.checkIntegrity(table);


        //start insert
        worker.put(table,
            ARIValue.of(tInt, 2),
            ARIValue.of(tChar, 'a'),
            ARIValue.of(tString, "dummy_data1"),
            ARIValue.of(tDbl, 1.2),
            ARIValue.of(tLong, 45L),
            ARIValue.of(tBool, false),
            ARIValue.of(tDate, new Date(System.currentTimeMillis())),
            ARIValue.of(tTime, new Timestamp(System.currentTimeMillis())),
            ARIValue.of(tBlob, new File("server.jks")),
            ARIValue.of(tFile, new File("server.jks"))
        );


        //start insert
        worker.put(table,
            ARIValue.of(tInt, 2),
            ARIValue.of(tChar, 'b'),
            ARIValue.of(tString, "dummy_data2"),
            ARIValue.of(tDbl, 45.4552),
            ARIValue.of(tLong, 45L),
            ARIValue.of(tBool, false),
            ARIValue.of(tDate, new Date(System.currentTimeMillis())),
            ARIValue.of(tTime, new Timestamp(System.currentTimeMillis())),
            ARIValue.of(tBlob, new File("server.jks")),
            ARIValue.of(tFile, new File("server.jks"))
        );


        //start insert
        worker.put(table,
            ARIValue.of(tInt, 3),
            ARIValue.of(tChar, 'b'),
            ARIValue.of(tString, "dummy_data3"),
            ARIValue.of(tDbl, 1.45),
            ARIValue.of(tLong, 45L),
            ARIValue.of(tBool, false),
            ARIValue.of(tDate, new Date(System.currentTimeMillis())),
            ARIValue.of(tTime, new Timestamp(System.currentTimeMillis())),
            ARIValue.of(tBlob, new File("server.jks")),
            ARIValue.of(tFile, new File("server.jks"))
        );

        //get
        ((SQLWorker)worker).get(table,null, new Event<ResultSet>() {

            @Override
            public void taskComplete(ResultSet rs) {
                try {
                    ResultSetMetaData metaData = rs.getMetaData();
                    for (int i = 0; i < metaData.getColumnCount(); i++){
                        System.out.println(metaData.getColumnName(i + 1));
                        System.out.println(metaData.getColumnTypeName(i + 1));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }, ARIValue.eq(tInt, 3));




    }
}