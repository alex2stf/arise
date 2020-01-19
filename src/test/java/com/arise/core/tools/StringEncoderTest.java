package com.arise.core.tools;

import org.junit.Test;

public class StringEncoderTest {

    @Test
    public void test(){
//        String rs = StringEncoder.encode("aaa", 0);
       for (int i = 0; i < 20; i++){
           System.out.println(StringEncoder.key("dasdasdas", i));
           System.out.println("");
       }
    }

}
