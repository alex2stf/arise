package com.arise.weland.impl;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class PCDeviceController implements IDeviceController {



    @Override
    public void update(Map<String, List<String>> queryParams) {
        Integer mouseX = getInt(queryParams, "mouseX");
        Integer mouseY = getInt(queryParams, "mouseX");

        update(mouseX, mouseY);

    }

    public Integer getInt(Map<String, List<String>> queryParams, String key){
        try {
            return Integer.valueOf(queryParams.get(key).get(0));
        }catch (Exception e){
           return null;
        }
    }

    public void update(Integer mouseX, Integer mouseY){
        Robot r = getRobot();
        if (r == null){
            return;
        }
        if (mouseX != null && mouseY != null){
            System.out.println("mouse mouse to " + mouseX + "&/" + mouseY);
            r.mouseMove(mouseX, mouseY);
        }
    }

    Robot robot;
    Robot getRobot(){
        if (robot == null){
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
                robot = null;
            }
        }
        return robot;
    }
}
