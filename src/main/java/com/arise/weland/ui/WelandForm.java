package com.arise.weland.ui;

import com.arise.canter.Registry;
import com.arise.core.AppSettings;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.arise.core.tools.StringUtil.join;
import static com.arise.weland.dto.DeviceStat.getInstance;

public class WelandForm extends JFrame implements Runnable {

    List<Provider> providers = new ArrayList<>();

    private static final int LARGE_FONT = 44;
    private static final int BIG_FONT = 30;


    public WelandForm(final Registry registry){
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        providers.add(new Provider() {
            @Override
            protected Font buildFont() {
                return new java.awt.Font("Tahoma", Font.ITALIC, BIG_FONT);
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

        final Set<String> addrs = getInstance().getIpv4Addrs();
        if (!CollectionUtil.isEmpty(addrs)){
            providers.add(new Provider() {
                @Override
                public String getText(Date date) {
                    return "IPv4: " + join(addrs, ",");
                }
            });
        }

        List<String> displays = AppSettings.getListWithPrefix("ui.display");
        for (final String d: displays){
            providers.add(new Provider() {
                @Override
                public String getText(Date date) {
                    return "" + registry.executeCmdLine(d);
                }
            });
        }

        if (getIcon() != null) {
            providers.add(new Provider() {
                @Override
                public String getText(Date date) {
                    return null;
                }

                @Override
                protected Container buildLabel() {
                    ImageIcon imageIcon = new ImageIcon(getIcon().getAbsolutePath());
                    JLabel imageLabel = new ImageLabel(imageIcon);
                    return imageLabel;
                }
            });
        }

        final JTextArea jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setEditable(false);
        jTextArea.setVisible(true);
        final JScrollPane scrollPane = new JScrollPane(jTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        Mole.addAppender(new Mole.Appender() {
            @Override
            public void append(String id, Mole.Bag bag, String text) {
                jTextArea.append(id + " " + bag + "] (" + sdf.format(new Date()) + " ) " + text + "\n");
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue( vertical.getMaximum()  + 1);
            }
        });

        Mole.getInstance(WelandForm.class).info("form loaded");
        providers.add(new Provider() {
            @Override
            public String getText(Date date) {
                return null;
            }

            @Override
            protected Container buildLabel() {
                return scrollPane;
            }
        });


        getContentPane().setLayout(new GridLayout(providers.size(), 1));

        for (Provider p: providers){
            if (p != null){
                getContentPane().add(p.getContainer());
            }
        }

        pack();
        run();
    }


    File getIcon(){
        File f = new File(AppSettings.getProperty(AppSettings.Keys.UI_IMAGE_ICON_PATH));
        if (f.exists()){
            return f;
        }
        return null;
    }


    @Override
    public void run() {
        Date date = new Date();
        for (Provider p: providers){
            Container container = p.getContainer();
            if(container instanceof JLabel) {
                JLabel label = (JLabel) container;
                String text = p.getText(date);
                if (text != null) {
                    label.setText(text);
                    label.setToolTipText(text);
                }
                else if (label instanceof ImageLabel && AppSettings.isTrue(AppSettings.Keys.UI_IMAGE_ICON_REFRESH)) {
                    ((ImageLabel)label).setImageIcon(getIcon().getAbsolutePath());
                }
            }
        }
    }

    private static abstract class Provider {

        Container label;

        public Provider(){
            label = buildLabel();
        }

        protected Container buildLabel(){
            JLabel label = new JLabel();
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            label.setFont(buildFont());
            label.setForeground(new Color(4, 2, 69));
            return label;
        }

        protected Font buildFont(){
            return new java.awt.Font("Tahoma", Font.BOLD, LARGE_FONT);
        }

        public  Container getContainer(){
            return label;
        }

        public abstract String getText(Date date);
    }


    public static class ImageLabel extends JLabel {
        private Image _myimage;
        int iW;
        int iH;

        public ImageLabel(ImageIcon _myImage){
            setImageIcon(_myImage);
        }

        public void setImageIcon(String path){
            try {
                setImageIcon(new ImageIcon(ImageIO.read(new File(path))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setImageIcon(ImageIcon imageIcon) {
            if (this._myimage != null){
                this._myimage.flush();
            }
            this._myimage = imageIcon.getImage();
            iW = imageIcon.getIconWidth();
            iH = imageIcon.getIconHeight();
            this.revalidate();
            this.repaint();
        }

        @Override
        public void paint(Graphics g){
            int ph = this.getHeight();
            g.drawImage(_myimage, 0, 0, iW * ph / iH, ph , null);
        }
    }


}
