package com.arise.weland.ui;

import com.arise.canter.CommandRegistry;
import com.arise.cargo.management.Locations;
import com.arise.core.models.Provider;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.AppDispatcher;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.impl.RadioPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.arise.core.AppSettings.*;
import static com.arise.core.tools.StringUtil.join;
import static com.arise.core.tools.Util.now;
import static com.arise.weland.dto.DeviceStat.getInstance;

public class ClockForm extends JFrame {

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    JTextField textField;

    public ClockForm(){

        setUndecorated(true);
        setBackground(new Color(0, 0, 1, 0));
//        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new CardLayout());
        textField = new JTextField();
        Font font = new Font("SansSerif", Font.BOLD, 80);
        textField.setFont(font);
        textField.setForeground(new Color(255, 255, 1));
        textField.setBackground(new Color(0, 0, 0, 0));
        textField.setHorizontalAlignment(SwingConstants.CENTER);

        add(textField);
        textField.setText(sdf.format(new Date()));

        AppDispatcher.onTick(new AppDispatcher.Event() {
            @Override
            public void execute() {
                refresh();
            }
        });

        ThreadUtil.repeatedTask(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 1000 * 60);
    }

    private void refresh(){
        try {
            textField.setText(sdf.format(new Date()));
            repaint();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }




}
