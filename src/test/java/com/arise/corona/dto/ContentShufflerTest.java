package com.arise.corona.dto;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ContentShufflerTest {

    @Test
    public void test(){
        ContentManager contentShuffler = new ContentManager();
        List<ContentInfo> contentInfos = new ArrayList<>();
        contentInfos.add(new ContentInfo().setVisited(0).setPath("A"));
        contentInfos.add(new ContentInfo().setVisited(1).setPath("B"));
        ContentManager.Playlist playlist = contentShuffler.getPlaylist("x", contentInfos);


        ContentInfo info = playlist.next();
        Assert.assertEquals("A", info.getName());

        System.out.println(info);


        info.setVisited(2);

        Assert.assertEquals("B", playlist.next().getName());
    }

}
