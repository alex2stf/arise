package com.arise.cargo;

import com.arise.cargo.impl.SpringWebClientHandler;
import com.arise.testclasses.SpringController;
import org.junit.Test;

public class WebClientGeneratorTest {


    @Test
    public void test(){
        WebClientGenerator clientGenerator = new WebClientGenerator().setMethodHandler(new SpringWebClientHandler());
        clientGenerator.scanClass(SpringController.class, "root");

        System.out.println(clientGenerator.toString());
    }

}
