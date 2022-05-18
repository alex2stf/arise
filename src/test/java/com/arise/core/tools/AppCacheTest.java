package com.arise.core.tools;




import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AppCacheTest {


    public static void test() throws IOException {
        String urlstr = "http://www.selenium.crm.orange.intra:8111/viewLog.html/doi/trei?buildId=265454&buildTypeId=OrangeMoney_SftpEngine_BuildRelease&tab=buildLog";
        URL url = new URL(urlstr);


        AppCache.putURL(url);
        Assert.assertEquals(url.toString(), AppCache.getURL("selenium.crm.orange.intra").toString());
        Assert.assertEquals(url.toString(), AppCache.getURL("viewLog.html/doi/trei").toString());
        Assert.assertEquals(url.toString(), AppCache.getURL("buildId=265454&buildTypeId=OrangeMoney_SftpEngine_BuildRelease&tab=buildLog").toString());

    }


    public static void testStoredList() throws IOException {
        List<String> x = new ArrayList<>();
        x.add("1");
        x.add("aa");
        x.add("bb");
        AppCache.storeList("test", x, 1);
        File f = AppCache.getStoredListFile("test");
        String content = FileUtil.read(f);
        System.out.println(content);

//        Assert.assertEquals("0\n1\naa\nbb\n", content);
        AppCache.StoredList storedList = AppCache.getStoredList("test");

        Assert.assertEquals(storedList.getIndex(), 1);
        Assert.assertEquals(storedList.getItems().get(1), "aa");
        Assert.assertEquals(storedList.getItems().get(0), "1");
        Assert.assertEquals(storedList.getItems().get(2), "bb");

    }

    public static void main(String[] args) throws IOException {
        AppCacheTest.testStoredList();
        AppCacheTest.test();
    }

}
