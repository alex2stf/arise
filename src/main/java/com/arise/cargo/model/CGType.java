package com.arise.cargo.model;

import com.arise.core.tools.StringUtil;

public class CGType extends CGContextAware {
    String name;
    private String requirement;
    private String nativeIdentifier;
    boolean iterable;
    String[] namespace;


    boolean abstractParameterType;

    public boolean isAbstractParameterType() {
        return abstractParameterType;
    }

    public CGType setAbstractParameterType(boolean abstractParameterType) {
        this.abstractParameterType = abstractParameterType;
        return this;
    }



    public CGType setTypedParameters(String[] typedParameters) {
        this.typedParameters = typedParameters;
        return this;
    }

    public boolean hasTypeParameters(){
        return typedParameters != null && typedParameters.length > 0;
    }

    public boolean isIterable() {
        return iterable;
    }

    public CGType setIterable(boolean iterable) {
        this.iterable = iterable;
        return this;
    }

    public String getName() {
        return name;
    }

    public CGType setName(String name) {
        this.name = name;
        return this;
    }

    public CGType setRequirement(String requirement) {
        this.requirement = requirement;
        return this;
    }

    public CGType setNativeIdentifier(String nativeIdentifier) {
        this.nativeIdentifier = nativeIdentifier;
        return this;
    }

    public String getRequirement() {
        return requirement;
    }

    public String getNativeIdentifier() {
        return nativeIdentifier;
    }

    public String getNativeName(){
        if (nativeIdentifier != null ){
            if(nativeIdentifier.indexOf(".") > -1) {
                String parts[] = nativeIdentifier.split("\\.");
                return parts[parts.length - 1];
            }
            return nativeIdentifier;
        }
        return null;
    }

    public String fullyQualifiedName(){
        if (nativeIdentifier != null){
            ParseUtil.Composition composition = ParseUtil.parse(nativeIdentifier);
            return composition.getName();
        }
        if (namespace != null && namespace.length > 0){
            return StringUtil.join(namespace, ".") + "." + getName();
        }
        return null;
    }

    @Override
    public String toString() {
        return "CGType{" +
                "name='" + name + '\'' +
                ", requirement='" + requirement + '\'' +
                ", nativeIdentifier='" + nativeIdentifier + '\'' +
                ", iterable=" + iterable +
                '}';
    }

    public CGType setNamespace(String[] namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getPackage(){
        if (namespace != null && namespace.length > 0) {
            return StringUtil.join(namespace, ".");
        }
        return "";
    }

    public String joinNamespace(String separator){
        if (namespace != null && namespace.length > 0) {
            return StringUtil.join(namespace, separator);
        }
        return "";
    }


    public CGType[] ctxImports(){
        return parentContext.calculateImports(this);
    }
}
