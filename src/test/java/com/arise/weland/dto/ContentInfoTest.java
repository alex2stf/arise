package com.arise.weland.dto;

import com.arise.core.tools.Assert;
import com.arise.core.tools.ContentType;

import static org.junit.jupiter.api.Assertions.*;

public class ContentInfoTest {

    public void testCsv(){
        ContentInfo contentInfo = new ContentInfo();
        contentInfo.setPath("test,\"name\"");
        contentInfo.setContentType(ContentType.APPLICATION_JSON);
        contentInfo.setPlaylist(Playlist.VIDEOS);
        contentInfo.setWidth(34);
        contentInfo.setHeight(78)
                .setDuration(355)
                .setPosition(589);
        System.out.println(contentInfo.toCsv());


        ContentInfo dec = ContentInfo.fromCsv(contentInfo.toCsv());
        assertEquals(dec.getPath(), contentInfo.getPath());
        assertEquals(dec.getTitle(), contentInfo.getTitle());
        assertEquals(dec.getWidth(), contentInfo.getWidth());
        assertEquals(dec.getHeight(), contentInfo.getHeight());
        assertEquals(dec.getPosition(), contentInfo.getPosition());
        assertEquals(dec.getDuration(), contentInfo.getDuration());

    }

    public static void main(String[] args) {

        ContentInfoTest contentInfoTest = new ContentInfoTest();
        contentInfoTest.testCsv();
    }
}