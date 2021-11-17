package com.arise.cargo.model;

import com.arise.core.tools.StringUtil;

public class ParseUtil {

    public static Composition parse(String input){
        String name = input;
        String[] typedParameters = new String[]{};
        if (input.indexOf("<") > -1){
            String prts[] = input.split("<");
            name = prts[0];
            if (prts[1].endsWith(">")){
                prts[1] = StringUtil.removeLastChar(prts[1]);
            }
            typedParameters = prts[1].split(",");
        }
        return new Composition(name, typedParameters);
    }

    public static class Composition extends CGParamType {
        private final String name;


        private Composition(String name, String[] typedParameters){
            this.name = name;
            this.typedParameters = typedParameters;
        }

        public String getName() {
            return name;
        }


    }
}
