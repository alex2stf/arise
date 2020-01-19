package com.arise.testclasses;

import com.arise.cargo.model.InputType;
import com.arise.cargo.model.UXField;

import java.util.Date;

public class TestDTO {

    @UXField(value = "name", type = InputType.TEXT)
    private String name;

    @UXField(value = "date", type = InputType.DATE)
    private Date date;

    private InnerDto innerDto;

    void doStuff(){

    }

    public class InnerDto{

//        String supplierName;

        void  inner(){
            doStuff();
        }
    }

    public static void main(String[] args) {
        System.out.println(
                InnerDto.class.getDeclaredFields()
        );
    }
}
