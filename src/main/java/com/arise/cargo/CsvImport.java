package com.arise.cargo;

import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvImport {

    public static StringBuilder all = new StringBuilder();

    public static void iterateFile(String source, Mapper mapper) throws IOException {
        String content = FileUtil.read(new File(source)).replaceAll("'", " ");
        StringBuilder sb = new StringBuilder();

        String lines[] = content.split("\n");
        String compile = StreamUtil.toString(FileUtil.findStream("insert.sql"));
        Whisker whisker = new Whisker();
        for (int j = 1; j < lines.length; j++){
            String line = lines[j];
//            System.out.println(line);
            String cols[] = line.split(",");
//            List<String>  actual = new ArrayList<>();
//            for (int i = 0; i < cols.length; i++){
//                if (StringUtil.hasText(cols[i])){
//                    actual.add(cols[i].trim());
//                }
//                else {
//                   actual.add("xxxx");
//                }
//
//
//            }
            String[] cp = new String[cols.length];
            for (int i = 0; i < cols.length; i++){
                cp[i] = cols[i].trim().replaceAll("�", "");
                        //.replaceAll("\\s+", " ").trim();
                //if (cp[i] == ""){
                //    cp[i] = "xxxxxx";
                //}
            }



            Map map = mapper.map(cp);
            if (map != null) {
//                String query = whisker.compile(compile, map);
               String query =
                       "INSERT INTO MCC_CODE (ID, CODE,CAMPAIGN_NAME,MERCHANT_ID,TERMINAL_ID,TERMINAL_NAME,CREATED_DATE,MERCHANT_CATEGORY,MERCHANT_DESCRIPTION)\n" +
                               "    VALUES ( MCC_ID_SEQ.nextval, " + map.get("code") + ",'" + map.get("campaign") + "','"+map.get("mid")+"','"+map.get("tid")+"','"+map.get("terminal_name")+"',SYSDATE,'"+map.get("category")+"','"+map.get("description")+"');\n";
//               query = "INSERT INTO PARTNER_SHOP (ID, NAME, MERCHANT_ID, TERMINAL_ID, CREATED_DATE)\n" +
//                       "    VALUES (PARTNER_SHOP_ID_SEQ.nextval, '"+map.get("terminal_name")+
//                       "', '"+map.get("mid")+"', '"+map.get("tid")+"', CURRENT_TIMESTAMP);\n";
                sb.append(query).append("\n");
                System.out.println(query);
            }


        }

        String name = new File(new File(source).getParent()) .getName() + "-" + new File(source).getName();

        FileUtil.writeStringToFile(new File(name.replaceAll(".csv", "-") + "out.sql"), sb.toString());
        all.append(sb.toString()).append("\n");
    }




    public static  void readFolder(String folder, Mapper mapper) throws IOException {
        File fldr = new File(folder);
        File []files = fldr.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("csv");
            }
        });

        for (File f:  files){
            iterateFile(f.getAbsolutePath(), mapper);
        }
    }


    public static void main(String[] args) throws IOException {


        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\BCR.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[4].trim() + " " + cols[5].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[5].trim() );
                return map;
            }
        });


        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\BRD.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[4].trim() + " " + cols[5].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[5].trim() );
                return map;
            }
        });


        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\ING.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[0].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[5].trim() );
                return map;
            }
        });


        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\CREDIT_EUROPE_BANK.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[3].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[0].trim() + " "  + cols[4].trim() + " " + cols[5].trim() );
                return map;
            }
        });


        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\GARANTI_BANI.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[3].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[0].trim() + " " +cols[4].trim() + " " + cols[5].trim() );
                return map;
            }
        });


        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\RAIFFEISEN.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[3].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[0].trim() + " " +cols[4].trim() + " " + cols[5].trim() );
                return map;
            }
        });

        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\TRANSILVANIA.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[3].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[0].trim() + " " +cols[4].trim() + " " + cols[5].trim() );
                return map;
            }
        });


        iterateFile("C:\\Users\\alexandru2.stefan\\Desktop\\DesktopDocs\\__MCC_CODES\\octombrie_2020\\UNICREDIT.csv" , new Mapper() {
            @Override
            public Map map(String[] cols) {
                if (cols[1].startsWith("Urmeaz")){
                    return null;
                }
                Map map = new HashMap();
                map.put("code", "null");
                map.put("mid", cols[1].trim());
                map.put("tid", cols[2].trim());
                map.put("terminal_name", cols[3].trim());
                map.put("campaign", "ORANGE_PACKAGE");
                map.put("category", "n/a");
                map.put("description", cols[0].trim() + " " +cols[4].trim() + " " + cols[5].trim() );
                return map;
            }
        });






        FileUtil.writeStringToFile(new File("all.sql"), all.toString());



    }

    private  interface Mapper {
        Map map(String[] cols);
    }
}
