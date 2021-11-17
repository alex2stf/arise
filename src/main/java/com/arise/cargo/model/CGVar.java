package com.arise.cargo.model;

import com.arise.cargo.Context;

import java.lang.reflect.Field;
import java.util.List;

import static com.arise.core.tools.StringUtil.hasText;

public class CGVar extends CGContextAware {

    private String label;
    private String description;
    private String type;
    private boolean readonly;
    private String id;
    private String name;
    private Object val;
    private String alias;
    private Integer maxlength;
    private Integer minlength;

    boolean allowGetter = true;
    boolean allowSetter = true;
    boolean isFinal;
    boolean isStatic;
    boolean isTranzient;
    boolean isPrimaryKey;
    boolean unique;
    String fetchType;
    String defaultValue;
    boolean nullable;
    boolean isArray;
    private CGClass containerClass;
    private InputType inputType;
    List<CGVar> variables;


    public CGVar(){

    }



    public CGVar(UXField uxField, Field field, Object obj, String id){
        this.id = (hasText(id)? id + "_" : "" );
        this.id += (field.getDeclaringClass() + "_" + field.getName() )
                .replaceAll("class", "")
                .replaceAll("\\s+", "")
                .replaceAll("\\.", "_");
        this.name = (hasText(uxField.value()) ? uxField.value() : field.getName());
        this.label = uxField.label();
        this.description = uxField.description();
        this.type = uxField.type().name().toLowerCase();
        this.val = obj;
        this.readonly = uxField.readonly();
        this.inputType = uxField.type();
    }

    @Override
    public CGVar setParentContext(Context parentContext) {
        return (CGVar) super.setParentContext(parentContext);
    }

    @Override
    public CGVar setCommentBlocks(List<String> arg) {
        return (CGVar) super.setCommentBlocks(arg);
    }

    @Override
    public CGVar setCommentLines(List<String> arg) {
        return (CGVar) super.setCommentLines(arg);
    }

    public CGClass getContainerClass() {
        return containerClass;
    }

    public CGVar setContainerClass(CGClass containerClass) {
        this.containerClass = containerClass;
        return this;
    }

    @Override
    public CGVar setAccessType(AccessType accessType) {
        return (CGVar) super.setAccessType(accessType);
    }

    public String getAlias() {
        return alias;
    }

    public CGVar setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public Integer getMaxlength() {
        return maxlength;
    }

    public CGVar setMaxlength(Integer maxlength) {
        this.maxlength = maxlength;
        return this;
    }

    public Integer getMinlength() {
        return minlength;
    }

    public CGVar setMinlength(Integer minlength) {
        this.minlength = minlength;
        return this;
    }



    public boolean isAllowGetter() {
        return allowGetter;
    }

    public CGVar setAllowGetter(boolean allowGetter) {
        this.allowGetter = allowGetter;
        return this;
    }

    public boolean isAllowSetter() {
        return allowSetter;
    }

    public CGVar setAllowSetter(boolean allowSetter) {
        this.allowSetter = allowSetter;
        return this;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public CGVar setFinal(boolean aFinal) {
        isFinal = aFinal;
        if (isFinal){
            readonly = true;
        }
        return this;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public CGVar setStatic(boolean aStatic) {
        isStatic = aStatic;
        return this;
    }

    public boolean isTranzient() {
        return isTranzient;
    }

    public CGVar setTranzient(boolean tranzient) {
        isTranzient = tranzient;
        return this;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public CGVar setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public CGVar setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public String getFetchType() {
        return fetchType;
    }

    public CGVar setFetchType(String fetchType) {
        this.fetchType = fetchType;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public CGVar setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public boolean isNullable() {
        return nullable;
    }

    public CGVar setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public boolean isArray() {
        return isArray;
    }

    public CGVar setArray(boolean array) {
        isArray = array;
        return this;
    }

    public String getId() {
        return id;
    }

    public CGVar setId(String id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public CGVar setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CGVar setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public CGVar setType(String type) {
        this.type = type;
        return this;
    }

    public boolean getReadonly() {
        return readonly;
    }

    public CGVar setReadonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public String getName() {
        return name;
    }

    public CGVar setName(String name) {
        this.name = name;
        return this;
    }

    public String ctxValue() {
        return parentContext.solveValue(this);
    }



    @Override
    public String toString() {
        return "CGVar{" +
                "label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", readonly=" + readonly +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", value='" + val + '\'' +
                ", alias='" + alias + '\'' +
                ", maxlength=" + maxlength +
                ", minlength=" + minlength +
                ", accessType=" + accessType +
                ", allowGetter=" + allowGetter +
                ", allowSetter=" + allowSetter +
                ", isFinal=" + isFinal +
                ", isStatic=" + isStatic +
                ", isTranzient=" + isTranzient +
                ", isPrimaryKey=" + isPrimaryKey +
                ", unique=" + unique +
                ", fetchType='" + fetchType + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", nullable=" + nullable +
                ", isArray=" + isArray +
                '}';
    }


    public Object getVal() {
        return val;
    }

    public String actualType(){
        return parentContext.calculateVariableType(this);
    }

    public CGVar setTypedParameters(String[] typedParameters) {
        this.typedParameters = typedParameters;
        return this;
    }

    public CGVar setInputType(InputType inputType) {
        this.inputType = inputType;
        return this;
    }

    public String ctxInputType(){
        return parentContext.solveInputTypeTag(inputType);
    }

    public boolean ctxRequiresComplexComposition(){
        return parentContext.requiresComplexComposition(this);
    }

    public InputType getInputType() {
        return inputType;
    }

    public List<CGVar> getVariables() {
        return variables;
    }

    public CGVar setVariables(List<CGVar> variables) {
        this.variables = variables;
        return this;
    }
}
