package com.arise.cargo;

import com.arise.cargo.model.*;
import com.arise.core.exceptions.SyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Context {
    private final String id;
    protected Set<CGType> types = new HashSet<>();
    protected Set<CGClass> classes = new HashSet<>();

    public CGClass currentClass;



    protected Context(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void registerType(CGType cgType){
        types.add(cgType);
    }


    public CGType getTypeByName(String name, CGParamType context){
        ParseUtil.Composition composition = ParseUtil.parse(name);
        name = composition.getName();

        for (CGType type: types){
            if (name.equals(type.getNativeIdentifier())){
                return type;
            }

            if (name.equals(type.getName())){
                return type;
            }
        }
        if (context != null && context.hasTypedParameters()){
            for (String s: context.getTypedParameters()){
                if (s.equals(name)){
                    return new CGType().setName(name).setAbstractParameterType(true);
                }
            }
        }
        throw new SyntaxException("type " + name + " not found");
    }

    public List<CGType> getTypesByName(String [] names, CGParamType context){

        List<CGType> res = new ArrayList<>();
        if (names == null || names.length < 1){
            return res;
        }
        for (String s: names){
            res.add(getTypeByName(s, context));
        }
        return res;
    }

    public CGClass getCurrentClass() {
        return currentClass;
    }

    public Context setCurrentClass(CGClass currentClass) {
        this.currentClass = currentClass;
        currentClass.setParentContext(this);

        classes.add(currentClass);
        registerType(currentClass);
        return this;
    }

    public abstract void compile();

    public abstract CGType[] calculateImports(Object any);

    public abstract String calculateVariableType(CGVar cgVar);

    public abstract String solveKeyword(CGClass cgClass);

    public abstract String solveAccessType(CGContextAware cgClass);

    public abstract List<CGMethod> calculateGetters(CGClass cgClass);

    public abstract String calculateArgumentsLine(CGMethod cgMethod);

    public abstract List<CGVar> solveNonStaticVars(CGClass cgClass);

    public abstract List<CGMethod> calculateSetters(CGClass cgClass);


    public String solveInputTypeTag(InputType inputType){
        return inputType.name().toLowerCase();
    };

    public boolean requiresComplexComposition(CGVar cgVar){
        return false;
    };

    public String solveValue(CGVar cgVar){
        return cgVar.getVal() != null ? String.valueOf(cgVar.getVal()) : null;
    };
}
