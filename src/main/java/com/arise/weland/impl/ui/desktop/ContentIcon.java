package com.arise.weland.impl.ui.desktop;

import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoProvider;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ContentIcon extends JButton {
    public ContentIcon(ContentInfo info, ContentInfoProvider contentInfoProvider) {
        setText(info.getTitle());

        if (StringUtil.hasText(info.getThumbnailId())){
            byte [] bytes = contentInfoProvider.getDecoder().getThumbnail(info.getThumbnailId());
            setIcon(new ImageIcon(bytes));
//            try {
//                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
