package com.arise.cargo;

import java.util.ArrayList;
import java.util.List;

public class ARIMethod {

    final String returnTypeName;
    final String name;

    final String[] parameterNames;
    final String[] parameterTypeNames;

    public ARIMethod( String name, String returnTypeName, String[] parameterNames, String[] parameterTypeNames) {
        this.returnTypeName = returnTypeName;
        this.name = name;
        this.parameterNames = parameterNames;
        this.parameterTypeNames = parameterTypeNames;
    }

    public ARIType getReturnType(Context context){
        return context.getTypeByName(returnTypeName);
    }

    public String getName() {
        return name;
    }

    public List<Arg> getArguments(Context context){
        List<Arg> args = new ArrayList<>();
        for (int i = 0; i < parameterNames.length; i++) {
            args.add(new Arg(context.getTypeByName(parameterTypeNames[i]), parameterNames[i]));
        }
        return args;
    }

    public boolean hasArguments(Context context) {
        return !getArguments(context).isEmpty();
    }

    public boolean isGetter(){
        return this instanceof Getter;
    }

    public boolean isSetter(){
        return this instanceof Setter;
    }


    public static class Arg {
        final ARIType type;
        final String name;

        public Arg(ARIType type, String name) {
            this.type = type;
            this.name = name;
        }


        public ARIType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    public static class Sourced extends ARIMethod {
        private final ARIProp prop;
        public Sourced(String name, String returnTypeName, String[] parameterNames, String[] parameterTypeNames, ARIProp prop) {
            super(name, returnTypeName, parameterNames, parameterTypeNames);
            this.prop = prop;
        }

        public ARIProp source(){
            return prop;
        }
    }

    public static class Getter extends Sourced {




        public Getter(ARIProp prop, Context context) {
            super(context.buildGetterName(prop.getName()),
                prop.getTypeName(),
                new String[]{prop.getName()},
                new String[]{prop.getTypeName()},
                prop
            );
        }


    }

    public static class Setter extends Sourced {
        public Setter(ARIProp prop, Context context) {
            super(context.buildSetterName(prop.getName()),
                prop.getTypeName(),
                new String[]{prop.getName()},
                new String[]{prop.getTypeName()},
                prop
            );
        }
    }



}
