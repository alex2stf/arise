package com.arise.cargo;

import com.arise.cargo.contexts.JVMContext;

public class CargoTest {

    public void test(){

        Context jvmContext = new JVMContext();

        Cargo cargo = new Cargo()
                .addContext(jvmContext)
                .loadBasicTypes()
                .readFromFile("src/test/resources/_cargo_/hibernate_sample.cargo");
//                .readFromFile("src/test/resources/_cargo_/test_nou.cargo");

        jvmContext.compile();
    }
}
