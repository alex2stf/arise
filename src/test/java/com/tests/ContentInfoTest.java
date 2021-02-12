package com.tests;

import com.arise.weland.dto.ContentInfo;

import java.util.HashMap;
import java.util.Map;

public class ContentInfoTest {

    public static void main(String[] args) {
        ContentInfo contentInfo = new ContentInfo().setPath("//test spatiu ");
        System.out.println(contentInfo.toString());
        System.out.println(contentInfo.getPath());

        //System.out.println(ContentInfo.decodePath(contentInfo.toString()));
        Map<String, String> m = new HashMap<>();
        m.put("P", contentInfo.getPath());

        ContentInfo contentInfo1 = ContentInfo.fromMap(m);

        System.out.println(contentInfo1.toString());
        System.out.println(contentInfo1.getPath());


//        System.out.println(ContentInfo.encodePath(contentInfo1.getPath()));
        ContentInfo c3 = new ContentInfo();
        c3.setPath(ContentInfo.encodePath(contentInfo1.getPath()));

        System.out.println(c3.getPath());


    }
}
