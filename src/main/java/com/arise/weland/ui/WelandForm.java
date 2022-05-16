package com.arise.weland.ui;

import com.arise.core.AppSettings;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.DeviceStat;
import com.github.sarxos.webcam.ds.buildin.natives.Device;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.arise.core.tools.StringUtil.join;
import static com.arise.weland.dto.DeviceStat.getInstance;

public class WelandForm extends JFrame implements Runnable {

    List<Provider> providers = new ArrayList<>();

    public WelandForm(){
        setExtendedState(JFrame.MAXIMIZED_BOTH);
//        setAlwaysOnTop(true);

        providers.add(new Provider() {
            @Override
            protected Font buildFont() {
                return new java.awt.Font("Tahoma", Font.ITALIC, 44);
            }

            @Override
            public String getText(Date date) {
                return "----------------";
            }
        });

        providers.add(new Provider() {
            @Override
            protected Font buildFont() {
                return new java.awt.Font("Tahoma", Font.ITALIC, 44);
            }

            @Override
            public String getText(Date date) {
                return SYSUtils.getDeviceDetailsString();
            }
        });

        providers.add(new Provider() {
            @Override
            public String getText(Date date) {
                return new SimpleDateFormat("EEEEE dd MMMMM[MM] yyyy").format(date);
            }
        });

        providers.add(new Provider() {
            @Override
            public String getText(Date date) {
                return new SimpleDateFormat("HH:mm:ss").format(date);
            }
        });

        providers.add(new Provider() {
            @Override
            public String getText(Date date) {
                return "IPv4: " + join(getInstance().getIpv4Addrs(), ",");
            }
        });

        providers.add(new Provider() {
            @Override
            protected Font buildFont() {
                return new java.awt.Font("Tahoma", Font.ITALIC, 44);
            }

            @Override
            public String getText(Date date) {
                return "----------------";
            }
        });


        getContentPane().setLayout(new java.awt.GridLayout(providers.size(), 1));

        for (Provider p: providers){
            getContentPane().add(p.label);
        }

        List<String> displays = AppSettings.getListWithPrefix("ui.display");
        

        pack();
        run();
    }


    @Override
    public void run() {
        Date date = new Date();
        for (Provider p: providers){
            p.getLabel().setText(p.getText(date));
        }
    }

    private static abstract class Provider {
        JLabel label;

        public Provider(){
            label = buildLabel();
        }

        protected JLabel buildLabel(){
            label = new JLabel();
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            label.setFont(buildFont());
            return label;
        }

        protected Font buildFont(){
            return new java.awt.Font("Tahoma", Font.BOLD, 64);
        }

        public  JLabel getLabel(){
            return label;
        }

        public abstract String getText(Date date);
    }
}
