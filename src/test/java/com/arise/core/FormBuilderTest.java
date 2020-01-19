package com.arise.core;

import com.arise.cargo.FormBuilder;
import com.arise.testclasses.SimpleDTO;
import org.junit.Test;

import java.io.StringWriter;

public class FormBuilderTest {


    @Test
    public void test(){
        FormBuilder formBuilder = new FormBuilder();
        SimpleDTO simpleDTO = new SimpleDTO();
        simpleDTO.setId("eqweqweqwe");
        simpleDTO.longText = "xxxxxxxxxxxxxxxxxxx";
        StringWriter writer = new StringWriter();

        formBuilder.scan(simpleDTO);
        formBuilder.buildForm(writer, "text");
//                fo.buildForm(writer);


        System.out.println(writer.toString());

    }

}
