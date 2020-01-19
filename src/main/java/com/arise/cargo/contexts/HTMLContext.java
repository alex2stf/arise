package com.arise.cargo.contexts;

import com.arise.cargo.Context;
import com.arise.cargo.model.CGClass;
import com.arise.cargo.model.CGContextAware;
import com.arise.cargo.model.CGMethod;
import com.arise.cargo.model.CGType;
import com.arise.cargo.model.CGVar;
import com.arise.cargo.model.InputType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HTMLContext extends Context {
    public HTMLContext() {
        super("html");
    }

    @Override
    public void compile() {

    }

    @Override
    public CGType[] calculateImports(Object any) {
        return new CGType[0];
    }

    @Override
    public String calculateVariableType(CGVar cgVar) {
        return null;
    }

    @Override
    public String solveKeyword(CGClass cgClass) {
        return null;
    }

    @Override
    public String solveAccessType(CGContextAware cgClass) {
        return null;
    }

    @Override
    public List<CGMethod> calculateGetters(CGClass cgClass) {
        return null;
    }

    @Override
    public String calculateArgumentsLine(CGMethod cgMethod) {
        return null;
    }

    @Override
    public List<CGVar> solveNonStaticVars(CGClass cgClass) {
        return null;
    }

    @Override
    public List<CGMethod> calculateSetters(CGClass cgClass) {
        return null;
    }


    @Override
    public String solveInputTypeTag(InputType inputType) {
        if (InputType.TEXT_AREA.equals(inputType)){
            return "textarea";
        }
        return "input";
    }


    @Override
    public String solveValue(CGVar var) {
        //TODO format
        if (var.getInputType().equals(InputType.DATE) && var.getVal() != null){
            if (var.getVal() instanceof Date){
                return new SimpleDateFormat("yyyy-MM-dd").format(var.getVal());
            }
        }
        return super.solveValue(var);
    }

    @Override
    public boolean requiresComplexComposition(CGVar cgVar) {
        if (cgVar.getInputType().equals(InputType.TEXT_AREA)){
            return true;
        }
        return false;
    }
}
