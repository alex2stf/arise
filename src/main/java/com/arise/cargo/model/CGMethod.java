package com.arise.cargo.model;


import com.arise.cargo.Context;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CGMethod extends CGContextAware {
    String name;
    String returnType;
    Args[] args;
    List<String> lines = new ArrayList<>();




    @Override
    public CGMethod setParentContext(Context parentContext) {
        return (CGMethod) super.setParentContext(parentContext);
    }

    @Override
    public CGMethod setAccessType(AccessType accessType) {
        return (CGMethod) super.setAccessType(accessType);
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<String> getLines() {
        return lines;
    }

    public Args[] getArgs() {
        return args;
    }

    public CGMethod setName(String name) {
        this.name = name;
        return this;
    }

    public CGMethod setReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    public CGMethod setArguments(Args ... args) {
        this.args = args;
        return this;
    }

    public String ctxArgumentsLine(){
        return parentContext.calculateArgumentsLine(this);
    }




    public CGMethod addBodyLine(String s) {
        lines.add(s);
        return this;
    }

    public static class Args {
        private final String type;
        private final String name;

        public Args(String type, String name){

            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }
}
