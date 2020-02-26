package com.arise.weland.impl.ui.desktop;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.WelandClient;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoDecoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentIcon extends JButton {
    private BufferedImage master;
    private ContentInfo info;
    private ContentDisplayer parent;
    ContentInfoDecoder decoder;
    public ContentIcon(final ContentInfo info, final ContentDisplayer parent) {
        this.info = info;
        this.parent = parent;

        decoder = parent.contentInfoProvider.getDecoder();
        setBackground(new java.awt.Color(204, 204, 255));
        setHorizontalTextPosition(JButton.CENTER);
        setVerticalTextPosition(JButton.CENTER);
        setText(info.getTitle().replaceAll(" ", "\n"));


        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WelandClient.openFile(info.getPath(), parent.worker);
            }
        });

        placeThumbnail();
    }

    public void placeThumbnail(){
        if (info.getThumbnailId() != null){

            if (iconsCache.containsKey(info.getThumbnailId())) {
                setIcon(iconsCache.get(info.getThumbnailId()));
            } else {
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
            }
        }
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);


    }


    private static final Map<String, Icon> iconsCache = new ConcurrentHashMap<>();

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
