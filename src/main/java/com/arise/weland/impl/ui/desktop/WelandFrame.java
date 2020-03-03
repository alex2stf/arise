/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arise.weland.impl.ui.desktop;

import com.arise.core.tools.NetworkUtil;
import com.arise.weland.WelandClient;
import com.arise.weland.dto.Playlist;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.PCDecoder;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author alexandru2.stefan
 */
public class WelandFrame extends javax.swing.JFrame {
    public static final Color DARK_BLUE = new Color(23, 27, 74);
    public static final Color LIGHT_BLUE = new Color(178, 202, 233);
    public static final Font BIG_FONT_BOLD = new Font("Arial", Font.BOLD, 20);
    public static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 16);
    private ContentInfoProvider contentInfoProvider;

    public WelandFrame(ContentInfoProvider contentInfoProvider) {
        this.contentInfoProvider = contentInfoProvider;
    }



    /**
     * Creates new form WelandFrame
     */


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    public void initComponents() {

        tabContainer = new javax.swing.JTabbedPane();
        tabContainer.setFont(BIG_FONT_BOLD);
        URI uri = null;
        try {
            uri = new URI("http://localhost:8221/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        musicDisplayer = new ContentDisplayer(uri, Playlist.MUSIC, contentInfoProvider);
        videoDisplayer = new ContentDisplayer(uri, Playlist.VIDEOS, contentInfoProvider);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.OverlayLayout(getContentPane()));

        tabContainer.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        tabContainer.addTab("music", musicDisplayer);
        tabContainer.addTab("videos", videoDisplayer);

        final JPanel settings = new JPanel();
        GridLayout settingsLayout = new GridLayout(3, 0);
        settings.setLayout(settingsLayout);


        tabContainer.addTab("settings", settings);

        NetworkUtil.scanIPV4(new NetworkUtil.IPIterator() {
            @Override
            public void onFound(String ip) {
                JLabel label = new JLabel();
                label.setFont(BIG_FONT_BOLD);
                label.setText("http://" + ip + ":8221");
                settings.add(label);
                settingsLayout.setRows(settingsLayout.getRows() + 1);
            }
        });

        getContentPane().add(tabContainer);




    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WelandFrame welandFrame= new WelandFrame(new ContentInfoProvider(new PCDecoder()));
        welandFrame.setVisible(true);
        welandFrame.initComponents();
        welandFrame.pack();

    }

    public void fullscreen(){
        fullScreen(this, 1200, 800);
    }

    private ContentDisplayer musicDisplayer;
    private javax.swing.JTabbedPane tabContainer;
    private ContentDisplayer videoDisplayer;


    public static final void fullScreen(JFrame jFrame, int width, int height){
        try {
            java.awt.GraphicsEnvironment graphicsEnvironment = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            java.awt.GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
            java.awt.DisplayMode displayMode = device.getDisplayMode();
            width = displayMode.getWidth();
            height = displayMode.getHeight();
            jFrame.setSize(width, height);
        } catch (Throwable t){
            jFrame.setSize(width, height);
        }
    }
}
