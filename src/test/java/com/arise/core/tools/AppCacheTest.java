package com.arise.core.tools;



import java.io.IOException;
import java.net.URL;

public class AppCacheTest {


    public void test() throws IOException {
        String urlstr = "http://www.selenium.crm.orange.intra:8111/viewLog.html/doi/trei?buildId=265454&buildTypeId=OrangeMoney_SftpEngine_BuildRelease&tab=buildLog";
        URL url = new URL(urlstr);


        AppCache.putURL(url);
        Assert.assertEquals(url.toString(), AppCache.getURL("selenium.crm.orange.intra").toString());
        Assert.assertEquals(url.toString(), AppCache.getURL("viewLog.html/doi/trei").toString());
        Assert.assertEquals(url.toString(), AppCache.getURL("buildId=265454&buildTypeId=OrangeMoney_SftpEngine_BuildRelease&tab=buildLog").toString());

    }

    public static void main(String[] args) throws IOException {
        new AppCacheTest().test();
    }

}
