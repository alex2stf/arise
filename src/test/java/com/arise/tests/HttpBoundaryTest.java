package com.arise.tests;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.Multipart;
import com.arise.core.tools.Assert;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HttpBoundaryTest {


    public static void main(String[] args) {

        HttpRequest request = new HttpRequest();
        request.addHeader("Content-Type", "boundary=1234");


        System.out.println(request.getBoundary());

        String multipart = "This is the preamble.  It is to be ignored, though it \n" +
                "     is a handy place for mail composers to include an \n" +
                "     explanatory note to non-MIME compliant readers. \n" +
                "     --simple boundary \n" +
                "\n" +
                "     This is implicitly typed plain ASCII text. \n" +
                "     It does NOT end with a linebreak. \n" +
                "     --simple boundary \n" +
                "     Content-type: text/plain; charset=us-ascii \n" +
                "\n" +
                "     This is explicitly typed plain ASCII text. \n" +
                "     It DOES end with a linebreak. \n" +
                "\n" +
                "     --simple boundary-- \n" +
                "     This is the epilogue.  It is also to be ignored.";
        List<byte[]> bytes = Multipart.splitBytesByBoundary(multipart.getBytes(), "simple boundary");
        Assert.assertEquals(bytes.size(), 2);

//        for (byte[] b: bytes){
//            System.out.println("BLOCK\n" + new String(b) + "ENDBLOCK");
//        }
    }
}
