package com.arise.weland.impl.ui.desktop;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.WelandClient;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoDecoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.weland.impl.ui.desktop.WelandFrame.BIG_FONT_BOLD;
import static com.arise.weland.impl.ui.desktop.WelandFrame.LIGHT_BLUE;

public class ContentIcon extends JButton {
    private static final Map<String, Icon> iconsCache = new ConcurrentHashMap<>();
    ContentInfoDecoder decoder;
    private BufferedImage master;
    private ContentInfo info;
    private ContentDisplayer parent;


    public ContentIcon(final ContentInfo info, final ContentDisplayer parent) {
        this.info = info;
        this.parent = parent;

        decoder = parent.contentInfoProvider.getDecoder();
        setBackground(LIGHT_BLUE);



//        setHorizontalTextPosition(JButton.CENTER);
//        setVerticalTextPosition(JButton.CENTER);
//        setText(info.getTitle().replaceAll(" ", "\n"));

//        setVerticalTextPosition(BOTTOM);
//        setVerticalAlignment(BOTTOM);
//        setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//        setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
//        CardLayout cardLayout = new CardLayout();
//
//        setLayout(new CardLayout());

//        JLabel text = new JLabel();
//        text.setText(info.getTitle());
        setFont(BIG_FONT_BOLD);
        setText(info.getTitle());
        setHorizontalTextPosition(JButton.CENTER);
        setVerticalTextPosition(JButton.CENTER);

//
//        setIconTextGap(0);
//        addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                WelandClient.openFile(info.getPath(), parent.worker);
//            }
//        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                WelandClient.openFile(info.getPath(), parent.worker);
            }
        });

        setMargin(new java.awt.Insets(20, 20, 20, 20));
//        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        placeThumbnail();
    }



    public void placeThumbnail(){
        if (info.getThumbnailId() != null){
            if (iconsCache.containsKey(info.getThumbnailId())) {
                setIcon(iconsCache.get(info.getThumbnailId()));
            }
            else {
                byte data[] = decoder.getThumbnail(info.getThumbnailId());
                if (data == null){
                    return;
                }
                master = createImageFromBytes(data, info.getThumbnailId());
                if (master == null) {
                    return;
                }
                Icon icon = new ImageIcon(master);
                iconsCache.put(info.getThumbnailId(), icon);
                setIcon(icon);
                setForeground(Color.WHITE);
            }
        }
    }





    private BufferedImage createImageFromBytes(byte[] imageData, String id) {
        if(imageData == null){
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return  ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
