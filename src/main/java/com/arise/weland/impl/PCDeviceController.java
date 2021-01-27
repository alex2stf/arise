package com.arise.weland.impl;

import com.arise.core.tools.Mole;
import com.arise.core.tools.ProgressiveRGBGenerator;

import java.awt.*;
import java.awt.event.InputEvent;

public class PCDeviceController implements IDeviceController {

    int mouseX = 0;
    int mouseY = 0;

    private static final Mole log = Mole.getInstance(PCDeviceController.class);


    Robot getRobot() {
        try {
            return new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return null;
    }





    static final char MOUSE_X = 'x';
    static final char MOUSE_Y= 'y';
    static final char DOUBLE_CLICK = 'K';


    @Override
    public void digestBytes(byte[] bytes) {
//        System.out.println("DEVCTRL: " + bytes.length + " " + new String(bytes));
        char what = '\0';
        StringBuilder buff = new StringBuilder();
        for (int i = 1; i < bytes.length; i++){
            byte c = bytes[i];
            if (c == '='){
                what = (char)bytes[i -1];
            }
            else if (';' == c){
                updateProperty(what, buff.substring(1));
                buff = new StringBuilder();
            }
            else if ('>' == c){
                //new line received
                //updateUI();
            }
            else {
                buff.append((char)c);
            }
        }
        try {
            updateProperty(what, buff.substring(1));
        }catch (Throwable t){

        }

    }

    private void updateMouseMove() {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point b = a.getLocation();
        double x = b.getX();
        double y = b.getY();
        double nx = x - (mouseX > 0 ? 1 : -1);
        double ny = y - (mouseY > 0 ? 1 : -1);
        Robot robot = getRobot();
        if (robot != null) {
            getRobot().mouseMove((int) nx, (int) ny);
        }
    }

    Integer getInt(String x){
        Integer r = null;
        try {
           Float f = Float.valueOf(x);
           if (f != null){
               r = f.intValue();
           }
        } catch (Exception e){
            log.error("Ignore key [" + x + "]");
        }
        return r != null ? r : 0;
    }

    private void updateProperty(int what, String text) {
        switch (what){
            case MOUSE_X:{
                mouseX = getInt(text);
                break;
            }
            case MOUSE_Y: {
                mouseY = getInt(text);
            }
            case DOUBLE_CLICK:
               if ("1".equals(text)){
                   updateMouseMove();
                   Robot r = getRobot();
                   if (r != null) {
                       getRobot().mousePress(InputEvent.BUTTON1_MASK);
                       getRobot().mouseRelease(InputEvent.BUTTON1_MASK);
                   }
                   return;
               }
               break;
        }

        updateMouseMove();
    }



    public static void main(String[] args) throws Throwable {
//        java.awt.Robot robot = new java.awt.Robot();
//        int i = 0;
//        java.text.DateFormatSymbols ds = new DateFormatSymbols(Locale.getDefault());
//        ds.setWeekdays(new String[]{
//                "ce kkt de zi",
//                "Dum Duminica",
//                "Muje Luni",
//                "Gunoi Marti",
//                "Mrc Miercuri",
//                "J Joi",
//                "InainteDeWeekend Vineri",
//                "Atâââât Sâmbătă",
//        });
//        SimpleDateFormat z = new SimpleDateFormat("EEEEE", ds);
//        String root = "ne-am facut ca muncim %s";
//
//        while (true){
//            System.out.println(String.format(root, z.format(new Date())));
//            robot.mouseMove(i, i);
//            i++;
//            Thread.sleep(1000 * 60 * 3);
//        }

        ProgressiveRGBGenerator colorGenerator = new ProgressiveRGBGenerator(10, 2);
        while (true){
            System.out.println(colorGenerator.next(2));
            Thread.sleep(1000);
        }


    }

}
