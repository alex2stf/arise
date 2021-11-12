package com.arise.weland.utils;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class AppSettings {


    private static final Mole log = Mole.getInstance(AppSettings.class);

    private static final Properties applicationProperties;




    static {
        File expect;

        if (SYSUtils.isAndroid()){
            File dir = new File(FileUtil.findDocumentsDir(), "laynee-config");
            if (!dir.exists()){
                dir.mkdir();
            }
            expect = new File(dir, "application.properties");
        }
        else {
            expect = new File("application.properties");
        }

        Properties binProps = null;
        try {
            binProps = FileUtil.loadProps(FileUtil.findStream("weland/app-default.properties"));
        } catch (IOException e) {
            log.error("Binary corrupted, default properties not found", e);
            binProps = new Properties();
        }

        if (!expect.exists()){
            FileUtil.saveProps(binProps, expect, "weland first props save");
            applicationProperties = binProps;
            log.info("Init application properties 1st time");
        }
        else {
            Properties tmpProps;
            try {
                log.info("Loading properties from " + expect.getAbsolutePath());
                Properties svdProps = FileUtil.loadProps(expect);
                Enumeration<String> enums = (Enumeration<String>) binProps.propertyNames();
                boolean updated = false;
                while (enums.hasMoreElements()) {
                    String key = enums.nextElement();
                    if (!svdProps.containsKey(key)){
                        String value = binProps.getProperty(key);
                        svdProps.put(key, value);
                        log.info("Build offer new application property " + key + "=" + value);
                        updated = true;
                    }
                }

                if (updated){
                    FileUtil.saveProps(svdProps, expect, "updated at " + new Date());
                    log.info("Saved updated properties file");
                }
                tmpProps = svdProps;
            } catch (IOException e) {
                log.error("Failed to load application properties from file " + expect.getAbsolutePath(), e);
                tmpProps = binProps;
            }
            applicationProperties = tmpProps;

        }
    }


    public static Properties getApplicationProperties(){
        return applicationProperties;
    }


    public static List<File> getScannableLocations(){
        String val = applicationProperties.getProperty("scan.locations");
        String keys[] = val.split(",");
        List<File> r = new ArrayList<>();
        for (String s: keys){
            if ("music".equalsIgnoreCase(s)){
                r.add(FileUtil.findMusicDir());
            }
            else if ("videos".equalsIgnoreCase(s) || "movie".equalsIgnoreCase(s)){
                r.add(FileUtil.findMoviesDir());
            }
            else if ("downloads".equalsIgnoreCase(s) || "download".equalsIgnoreCase(s)){
                r.add(FileUtil.findDownloadDir());
            }
            else if(s.endsWith(":")){
                File d = getDrive(s);
                if (d != null && d.exists()){
                    r.add(d);
                }
                else {
                    File f = new File(s);
                    if (f.exists()){
                        r.add(f);
                    }
                }

            }
            else {
                File f = new File(s);
                if (f.exists()){
                    r.add(f);
                }
            }

        }
        return r;
    }


    private static File getDrive(String s){
        File drives[] = File.listRoots();
        for (File d: drives){
            if (d.getAbsolutePath().startsWith(s)){
                return d;
            }
        }
        return null;
    }


    public static String getProperty(Keys key){
        return applicationProperties.getProperty(key.s);
    }

    public static boolean isTrue(Keys key){
        if (applicationProperties.containsKey(key.s)){
            return "true".equalsIgnoreCase(applicationProperties.getProperty(key.s));
        }
        return false;
    }


    public enum Keys {
        PREFERRED_BROWSER("preferred.browser", new String[]{"selenium"}),
        SINGLE_INSTANCES("single.instances", new String[]{"true (default)", "false"});


        final String s;
        private final String[] variants;

        Keys(String s, String[] variants) {
            this.s = s;
            this.variants = variants;
        }
    }




}
