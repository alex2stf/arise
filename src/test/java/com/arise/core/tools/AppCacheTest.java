package com.arise.core.tools;


import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class AppCacheTest {


    @Test
    public void test() throws IOException {
        String urlstr = "http://www.selenium.crm.orange.intra:8111/viewLog.html/doi/trei?buildId=265454&buildTypeId=OrangeMoney_SftpEngine_BuildRelease&tab=buildLog";
        URL url = new URL(urlstr);


        AppCache.putURL(url);
        Assert.assertEquals(url.toString(), AppCache.getURL("selenium.crm.orange.intra").toString());
        Assert.assertEquals(url.toString(), AppCache.getURL("viewLog.html/doi/trei").toString());
        Assert.assertEquals(url.toString(), AppCache.getURL("buildId=265454&buildTypeId=OrangeMoney_SftpEngine_BuildRelease&tab=buildLog").toString());

    }

}
