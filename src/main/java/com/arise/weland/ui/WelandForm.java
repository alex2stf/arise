package com.arise.weland.ui;

import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.core.AppSettings;
import com.arise.core.models.Provider;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.FileUtil;
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

    List<TextProvider> prov = new ArrayList<>();


    private final static SimpleDateFormat sdf = new SimpleDateFormat("EEEEE dd MMMMM yyyy HH:mm:ss");



    List<Tuple2<ImageLabel, String>> imgs = new ArrayList<>();

    public WelandForm(final CommandRegistry commandRegistry){
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        addT(prov, new Provider<String>() {
            @Override
            public String get() {
                return SYSUtils.getDeviceDetailsString();
            }
        });

        addT(prov, new Provider<String>() {
            @Override
            public String get() {
                return sdf.format(new Date());
            }
        });


        addT(prov, new Provider<String>() {
            @Override
            public String get() {
                return "IPv4: " + join(getInstance().getIpv4Addrs(), ",");
            }
        });

        List<String> displays = AppSettings.getListWithPrefix("ui.extra.line");
        for (final String d: displays){
            addT(prov, new Provider<String>() {
                @Override
                public String get() {
                    return "" + commandRegistry.executeCmdLine(d);
                }
            });
        }


        getContentPane().setLayout(new GridLayout(0, 2));
        JPanel left = new JPanel();
        left.setLayout(new CardLayout());
        getContentPane().add(left);

        //start right pane:
        JPanel right = new JPanel();
        right.setLayout(new GridLayout(2, 0));
        getContentPane().add(right);

        JPanel to = new JPanel();
        final JScrollPane tl = new JScrollPane(to);
        tl.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tl.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        to.setLayout(new GridLayout(prov.size(), 0));
        right.add(tl);

        //add providers
        for (TextProvider p: prov){
            to.add(p.get().first());
        }

        //add logging area
        final Tuple2<JTextArea, JScrollPane> logTuple = buildScrollableTextArea();
        right.add(logTuple.second());

        final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        Mole.addAppender(new Mole.Appender() {
            @Override
            public void append(String id, Mole.Bag bag, String text) {
                logTuple.first().append(id + " " + bag + "] (" + sdf.format(new Date()) + " ) " + text + "\n");
                JScrollBar vertical = logTuple.second().getVerticalScrollBar();
                vertical.setValue( vertical.getMaximum()  + 1);
            }
        });

        //add images:

        List<String> ics = AppSettings.getListWithPrefix("ui.image.icon");
        JTabbedPane tabs = new JTabbedPane();

        for (int i = 0; i < ics.size(); i++){
            File f = new File(ics.get(i));
            ImageIcon imageIcon = new ImageIcon(f.getAbsolutePath());
            ImageLabel imageLabel = new ImageLabel(imageIcon);
            tabs.add("Icn" + i, imageLabel);
            imgs.add(new Tuple2<>(imageLabel, f.getAbsolutePath()));
        }
        left.add(tabs);

        pack();
        run();
    }

    Tuple2<JTextArea, JScrollPane> buildScrollableTextArea(){
        final JTextArea t = new JTextArea();
        t.setLineWrap(true);
        t.setEditable(false);
        t.setVisible(true);
        final JScrollPane s = new JScrollPane(t);
        s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        s.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return new Tuple2<>(t, s);
    }




    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();

        for (TextProvider tp: prov){
            tp.get().first().setText(
                    tp.get().second().get()
            );
        }

        for (Tuple2<ImageLabel, String> tpl: imgs){
            tpl.first().setImageIcon(tpl.second());
        }
//        for (Provider<String> p: providers){
//            sb.append(p.get() + "\n");
//
////            Container container = p.getContainer();
////            if(container instanceof JLabel) {
////                JLabel label = (JLabel) container;
////                String text = p.getText(date);
////                if (text != null) {
////                    label.setText(text);
////                    label.setToolTipText(text);
////                }
////                else if (label instanceof ImageLabel && AppSettings.isTrue(AppSettings.Keys.UI_IMAGE_ICON_REFRESH)) {
////                    ((ImageLabel)label).setImageIcon(getIcon().getAbsolutePath());
////                }
////            }
//        }
//        System.out.println("change");
//        jLabel.setText(sb.toString());
    }




    private static void addT(List<TextProvider> l, final Provider<String> p){
        l.add(new TextProvider() {
            @Override
            public Provider<String> getp() {
                return p;
            }
        });
    }


    public static abstract class TextProvider implements Provider<Tuple2<JLabel, Provider<String>>> {

        final JLabel l = new JLabel();
        @Override
        public Tuple2<JLabel, Provider<String>> get() {

            l.setText(getp().get());
            l.setHorizontalAlignment(SwingConstants.CENTER);
            return new Tuple2<>(l, getp());
        }


        public abstract Provider<String> getp();
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
