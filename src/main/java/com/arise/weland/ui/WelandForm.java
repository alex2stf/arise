package com.arise.weland.ui;

import com.arise.canter.CommandRegistry;
import com.arise.cargo.management.Locations;
import com.arise.core.AppSettings;
import com.arise.core.models.Provider;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.weland.impl.RadioPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.arise.core.AppSettings.*;
import static com.arise.core.AppSettings.isFalse;
import static com.arise.core.tools.StringUtil.join;
import static com.arise.weland.dto.DeviceStat.getInstance;

public class WelandForm extends JFrame implements Runnable {

    List<TextProvider> prov = new ArrayList<>();


    private final static SimpleDateFormat sdf = new SimpleDateFormat("EEEEE dd MMMMM yyyy HH:mm:ss");



    List<ImageLabel> imgs = new ArrayList<>();
    JTabbedPane tabs = null;

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

        List<String> displays = getListWithPrefix("ui.extra.line");
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

        List<String> ics = getListWithPrefix("ui.image.icon");

        tabs = new JTabbedPane();

        refreshSnapshot();

        for (int i = 0; i < ics.size(); i++){
            File f = new File(ics.get(i));
            ImageLabel imageLabel = new ImageLabel(f.getAbsolutePath());
            tabs.add("Icn" + i, imageLabel);
            imgs.add(imageLabel);
        }
        left.add(tabs);

        pack();
        run();
        toFront();
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
        for (TextProvider tp: prov){
            tp.get().first().setText(
                    tp.get().second().get()
            );
        }
        for (ImageLabel l: imgs){
            l.refresh();
        }
    }




    private static void addT(List<TextProvider> l, final Provider<String> p){
        l.add(new TextProvider() {
            @Override
            public Provider<String> getp() {
                return p;
            }
        });
    }

    boolean tC(String t){
        for (int i = 0; i < tabs.getTabCount(); i++){
            if (tabs.getTitleAt(i).equalsIgnoreCase(t)){
                return true;
            }
        }
        return false;
    }

    public void refreshSnapshot() {
        if (tabs == null || isFalse(Keys.UI_INCLUDE_SNAPSHOTS)){
            return;
        }
        File f[] = Locations.snapshots().listFiles();
        if (f != null && f.length > 0){
            for (int i = 0; i < f.length; i++){
                ImageLabel iL = new ImageLabel(f[i].getAbsolutePath());
                if (!tC(f[i].getName())){
                    tabs.add(f[i].getName(), iL);
                    imgs.add(iL);
                }
            }
        }
        for (ImageLabel i: imgs){
            i.refresh();
        }
    }

    public void setRadioPlayer(RadioPlayer rplayer) {

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
        final String _p;

        public ImageLabel(String p){
            _p = p;
            refresh();
        }

        public void refresh(){
            try {
                setImageIcon(new ImageIcon(ImageIO.read(new File(_p))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        private void setImageIcon(ImageIcon imageIcon) {
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
