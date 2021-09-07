package com.arise.core.tools;

import com.arise.core.tools.models.FoundHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilTest {

    void testCase1() throws IOException {
        StringReader stringReader = new StringReader("line1\nline2\nline3");
        List<String> lines = FileUtil.readLines(stringReader);
        Assert.assertEquals(3, lines.size());
        Assert.assertEquals("line1", lines.get(0));
        Assert.assertEquals("line2", lines.get(1));
        Assert.assertEquals("line3", lines.get(2));
    }


    public static void main(String[] args) throws IOException {
        new FileUtilTest().testCase1();
    }
}