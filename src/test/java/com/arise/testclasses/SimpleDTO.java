package com.arise.testclasses;

import com.arise.cargo.model.InputType;
import com.arise.cargo.model.UXField;

import java.util.List;

public class SimpleDTO {
    @UXField(value = "str", label = "str label", description = "some description")
    private String str;

    @UXField(value = "passwd", label = "passwd label", description = "passwd description", type = InputType.PASSWORD)
    private String passwd;

    @UXField(readonly = true, value = "id")
    private String id;


    @UXField(value = "str", label = "str label", description = "some description", type = InputType.TEXT_AREA)
    public String longText;

    @UXField(value = "inner", type = InputType.OBJECT)
    private TestDTO inner;


    @UXField(value = "list", type = InputType.LIST)
    private List<TestDTO> list;


    public String getStr() {
        return str;
    }

    public SimpleDTO setStr(String str) {
        this.str = str;
        return this;
    }

    public String getPasswd() {
        return passwd;
    }

    public SimpleDTO setPasswd(String passwd) {
        this.passwd = passwd;
        return this;
    }

    public String getId() {
        return id;
    }

    public SimpleDTO setId(String id) {
        this.id = id;
        return this;
    }
}
