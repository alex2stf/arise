package com.arise.weland.dto;

import com.arise.core.tools.MapUtil;

import java.util.List;
import java.util.Map;


/**
 * hide youtube ads video:
 * document.getElementById('player-container-outer').style.visibility='hidden'
 *
 *
 * skip ad:
 * document.getElementsByClassName('ytp-ad-skip-button-slot')[0].click()
 *
 * play video:
 * document.getElementsByClassName('ytp-play-button ytp-button')[0].click()
 */
public class DeviceCtrlCmd {

    int mx = 0;
    int my = 0;
    public void digest(Map<String, List<String>> params) {

    }

    public int mouseX() {
        return mx;
    }

    public int mouseY() {
        return my;
    }


    String getParam(String name, Map<String, List<String>> params){
        if (params.containsKey(name)){
            return params.get(name).get(0);
        }
        return null;
    }



}
