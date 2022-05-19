package com.arise.weland.dto;

import com.arise.core.tools.ContentType;

import static com.arise.core.tools.Assert.assertEquals;


public class ContentInfoTest {

    public static void testCsv(){
        ContentInfo contentInfo = new ContentInfo();
        contentInfo.setPath("test,\"name\"");
        contentInfo.setContentType(ContentType.APPLICATION_JSON);
        contentInfo.setPlaylist(Playlist.VIDEOS);
        contentInfo.setWidth(34);
        contentInfo.setHeight(78)
                .setDuration(355)
                .setPosition(589);
        assertEquals("test\\,\"name\",_,_,_,_,0,_,V,34,78,589,355,_,APPLICATION_JSON", contentInfo.toCsv());


        ContentInfo dec = ContentInfo.fromCsv(contentInfo.toCsv());
        assertEquals(dec.getPath(), contentInfo.getPath());
        assertEquals(dec.getTitle(), contentInfo.getTitle());
        assertEquals(dec.getWidth(), contentInfo.getWidth());
        assertEquals(dec.getHeight(), contentInfo.getHeight());
        assertEquals(dec.getPosition(), contentInfo.getPosition());
        assertEquals(dec.getDuration(), contentInfo.getDuration());

    }

    public static void main(String[] args) {
        ContentInfoTest.testCsv();
    }
}