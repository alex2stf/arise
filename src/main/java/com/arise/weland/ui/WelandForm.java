package com.arise.weland.ui;

import com.arise.core.tools.NetworkUtil;

import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WelandForm extends JFrame implements Runnable {

    List<Provider> providers = new ArrayList<>();

    public WelandForm(){
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setAlwaysOnTop(true);

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

        final String ip = NetworkUtil.getCurrentIPV4AddressSync();
        providers.add(new Provider() {
            @Override
            public String getText(Date date) {
                return "IPv4: " + ip;
            }
        });

        getContentPane().setLayout(new java.awt.GridLayout(providers.size(), 1));

        for (Provider p: providers){
            getContentPane().add(p.label);
        }

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
            label = new JLabel();
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            label.setFont(new java.awt.Font("Tahoma", 0, 44)); // NOI18N
        }

        public  JLabel getLabel(){
            return label;
        }

        public abstract String getText(Date date);
    }
}
