package com.arise.astox.clib;

import java.io.*;


/**
 * Created by alex on 08/08/2017.
 */
public class LibLoader {


    private static volatile boolean loaded = false;


    public static synchronized void load(String path){

        try {
            System.loadLibrary(path);
        } catch (UnsatisfiedLinkError error){
            System.out.println("System.loadLibrary("+path+") failed with " + error.getMessage());
           try{
                    loadNative(path);
               } catch (Exception e){
                   e.printStackTrace();
                   System.exit(-1);
               }
            }
    }

    private static synchronized String rloc(String z) throws IOException {
        InputStream i = LibLoader.class.getClassLoader().getResourceAsStream(z);

        File f = new File(z);
        if (i != null) {
            byte[] buffer = new byte[1024];
            int readBytes;

            if(!f.exists()){
                if(!f.createNewFile()){
                    System.out.println("failed to create file " + f.getAbsolutePath());
                    return null;
                }
            }
            FileOutputStream os = new FileOutputStream(f);
            System.out.println("relocate file " + f.getAbsolutePath());
            try {
                while ((readBytes = i.read(buffer)) != -1) {
                    os.write(buffer, 0, readBytes);
                }
            }
            finally {
                os.close();
                i.close();
                return f.getAbsolutePath();
            }
        } else {
            System.out.println("File " + z + " was not found inside JAR.");
        }
        return null;
    }

    private static synchronized void loadNative(String p) throws Exception {


        String e[] = {p +".dll", "lib" + p + ".so"};
        String ps[] = new String[e.length];


        for (int i = 0; i < e.length; i++){
            ps[i] = rloc(e[i]);
        }

        try {
            System.loadLibrary(p);
        } catch (UnsatisfiedLinkError ex){
            System.out.println("failed to load library " + p + " after relocation");
            String osname = System.getProperty("os.name");
            System.out.println("osname = " + osname);

            if (osname != null && osname.toLowerCase().startsWith("windows")){
                System.out.println("System.load(" + ps[0] + ")");
                System.load(ps[0]);
            } else {
                System.out.println("System.load(" + ps[1] + ")");
                System.load(ps[1]);
            }
        }

    }
}
