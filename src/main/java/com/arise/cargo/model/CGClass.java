package com.arise.cargo.model;

import com.arise.cargo.Context;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;

import static com.arise.core.tools.StringUtil.join;

public class CGClass extends CGType {

    String[] extend;
    String[] implement;
    AccessType accessType;
    boolean isAbstract;
    ClassFlavour flavour;
    String schema;
    String table;
    boolean persistable;
    boolean embeddable;
    boolean isFinal;
    boolean iterable;


    List<CGVar> varList = new ArrayList<>();


    @Override
    public CGClass setTypedParameters(String[] typedParameters) {
        return (CGClass) super.setTypedParameters(typedParameters);
    }

    public String[] getExtend() {
        return extend;
    }

    public CGClass setExtend(String[] extend) {
        this.extend = extend;
        return this;
    }

    public String[] getImplement() {
        return implement;
    }

    public CGClass setImplement(String[] implement) {
        this.implement = implement;
        return this;
    }


    @Override
    public CGClass setCommentBlocks(List<String> arg) {
        return (CGClass) super.setCommentBlocks(arg);
    }

    @Override
    public CGClass setCommentLines(List<String> arg) {
        return (CGClass) super.setCommentLines(arg);
    }

    @Override
    public CGClass setAccessType(AccessType accessType) {
        return (CGClass) super.setAccessType(accessType);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public CGClass setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
        return this;
    }

    public ClassFlavour getFlavour() {
        return flavour;
    }

    public CGClass setFlavour(ClassFlavour flavour) {
        this.flavour = flavour;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public CGClass setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getTable() {
        return table;
    }

    public CGClass setTable(String table) {
        this.table = table;
        return this;
    }


    @Override
    public CGClass setName(String name) {
        return (CGClass) super.setName(name);
    }

    public boolean isPersistable() {
        return persistable;
    }

    public CGClass setPersistable(boolean persistable) {
        this.persistable = persistable;
        return this;
    }

    public boolean isEmbeddable() {
        return embeddable;
    }

    public CGClass setEmbeddable(boolean embeddable) {
        this.embeddable = embeddable;
        return this;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public CGClass setFinal(boolean aFinal) {
        isFinal = aFinal;
        return this;
    }

    public boolean isIterable() {
        return iterable;
    }

    public CGClass setIterable(boolean iterable) {
        this.iterable = iterable;
        return this;
    }



    @Override
    public String toString() {
        return "CGClass{" +
                "typedParameters=[" + join(typedParameters, ",") +
                "], extend=[" + join(extend, ",") +
                "], implement=[" + join(implement, ",") +
                "], accessType=" + accessType +
                ", isAbstract=" + isAbstract +
                ", flavour=" + flavour +
                ", schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                ", name='" + name + '\'' +
                ", persistable=" + persistable +
                ", embeddable=" + embeddable +
                ", isFinal=" + isFinal +
                ", iterable=" + iterable +
                '}';
    }

    public boolean hasProperty(String name) {
        for (CGVar var : varList) {
            if (name.equals(var.getName())) {
                return true;
            }
        }
        return false;
    }

    public CGClass addProperty(CGVar cgVar) {
        cgVar.setParentContext(this.parentContext);
        varList.add(cgVar);
        cgVar.setContainerClass(this);
        return this;
    }

    @Override
    public CGClass setNamespace(String[] namespace) {
        return (CGClass) super.setNamespace(namespace);
    }

    public List<CGVar> getProperties() {
        return varList;
    }




    public String simpleClassName(){
        return name;
    }





    @Override
    public CGClass setParentContext(Context parentContext) {
        for (CGVar cgVar: varList){
            cgVar.setParentContext(parentContext);
        }
        return (CGClass) super.setParentContext(parentContext);
    }

    public String ctxDisplayKeyword(){
        return parentContext.solveKeyword(this);
    }


    public boolean hasExtends(){
        return extend != null && extend.length > 0;
    }

    public boolean hasImplements(){
        return implement != null && implement.length > 0;
    }

    public String getImplementsCSV(){
        if (hasImplements()) {
            return StringUtil.toCSV(implement);
        }
        return null;
    }

    public String getExtendsCSV(){
        if (hasExtends()) {
            return StringUtil.toCSV(extend);
        }
        return null;
    }

    public String classParameterTypesComma(){
        if (hasTypeParameters()){
            return StringUtil.join(typedParameters, ", ");
        }
        return null;
    }

    public List<CGVar> ctxVarsNonStatic(){
        return parentContext.solveNonStaticVars(this);
    }






    public List<CGMethod> ctxPublicGetterMethods(){
        return parentContext.calculateGetters(this);
    }

    public List<CGMethod> ctxPublicSetterMethods(){
        return parentContext.calculateSetters(this);
    }


}
