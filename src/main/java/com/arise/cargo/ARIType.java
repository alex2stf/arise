package com.arise.cargo;

import static com.arise.cargo.Cargo.calculateComp;
import static com.arise.cargo.Cargo.createClassId;


import com.arise.cargo.Cargo.Comp;
import com.arise.core.tools.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ARIType extends ARILined {


    protected String name;
    protected String[] namespaces;
    protected String id;
    private boolean iterable;
    private String[] diamondTypes;


    private List<ARIMethod> methods = new ArrayList<>();
    protected List<ARIProp> properties = new ArrayList<>();
    private List<ARIType> diamondImplementations = new ArrayList<>();
    private String requirement;

    public static ARIType simple(String name) {
        return new ARIType(name, null, false, null);
    }

    public ARIType(String name, String[] namespaces, boolean iterable, String[] diamondTypes) {
        this.name = name;
        this.namespaces = namespaces;
        this.iterable = iterable;
        this.diamondTypes = diamondTypes;
        this.id = createClassId(name, namespaces);
    }

    public ARIType(String name, String[] namespaces, boolean iterable) {
        Comp c = calculateComp(name, 0);
        this.name = c.name;
        this.diamondTypes = c.diamonds;
        this.namespaces = namespaces;
        this.iterable = iterable;
        this.id = createClassId(name, namespaces);
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    void setDiamondImplementations(List<ARIType> diamondImplementations) {
        this.diamondImplementations = diamondImplementations;
    }

    public List<ARIType> getCurrentDiamondImplementations() {
        return diamondImplementations;
    }

    public List<ARIProp> getProperties() {
        Collections.sort(properties);
        return properties;
    }



    public void setProperties(List<ARIProp> properties) {
        this.properties = properties;
    }

    public void addProperty(ARIProp p){
        assert p.getParent() == this;
        properties.add(p);
    }

    public List<ARIMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<ARIMethod> methods) {
        this.methods = methods;
    }

    public void addMethod(ARIMethod method) {
        this.methods.add(method);
    }

    public boolean hasDiamonds(){
        return diamondTypes != null && diamondTypes.length > 0;
    }

    public boolean isIterable() {
        return iterable;
    }

    public String getName() {
        return name;
    }

    public String[] getNamespaces() {
        return namespaces;
    }

    public String getId(){
        return id;
    }

    public String packId(){
        return (namespaces != null && namespaces.length > 0) ? StringUtil.join(namespaces, ".") : "";
    }

    @Override
    public String toString() {
        return id;
    }

    public List<ARIProp> getFinalNonStaticProperties() {
        List<ARIProp> r = new ArrayList<>();
        for (ARIProp p: getProperties()){
            if (p.isFinal() && !p.isStatic()){
                r.add(p);
            }
        }
        Collections.sort(r);
        return r;
    }

    public List<ARIProp> getFinalNonStaticProperties(AccessType accessType) {
        List<ARIProp> r = new ArrayList<>();
        for (ARIProp p: getProperties()){
            if (p.isFinal() && !p.isStatic()){
                if (accessType.equals(p.getAccessType())) {
                    r.add(p);
                }
            }
        }
        Collections.sort(r);
        return r;
    }

    public List<ARIProp> getNonFinalAndNonStaticProperties(AccessType accessType) {
        List<ARIProp> r = new ArrayList<>();
        for (ARIProp p: getProperties()){
            if (!p.isFinal() && !p.isStatic()){
                if (accessType.equals(p.getAccessType())) {
                    r.add(p);
                }
            }
        }
        return r;
    }

    public List<ARIProp> getFinalStaticProperties(AccessType accessType) {
        List<ARIProp> r = new ArrayList<>();
        for (ARIProp p: getProperties()){
            if (p.isFinal() && p.isStatic()){
                if (accessType.equals(p.getAccessType())) {
                    r.add(p);
                }
            }
        }
        return r;
    }



    protected boolean hasNonStaticFinals() {
        for (ARIProp cProp: getProperties()){
            if (cProp.isFinal() && !cProp.isStatic()){
                return true;
            }
        }
        return false;
    }


    public String[] getDiamondNames() {
        String[] r = new String[diamondTypes.length];
        for (int i = 0; i < diamondTypes.length; i++){
            r[i] = Context.alpha[i];
        }
        return r;
    }

    public boolean hasProps(AccessType accessType){
        for (ARIProp prop: properties){
            if (accessType.equals(prop.getAccessType())){
                return true;
            }
        }
        return false;
    }





    public ARIProp getPropertyByName(String name){
        for (ARIProp p: properties){
            if (name.equals(p.getName())){
                return p;
            }
        }
        return null;
    }

    public List<ARIProp> getByAccessType(AccessType accessType){
        List<ARIProp> p = new ArrayList<>();
        for (ARIProp prop: properties){
            if (accessType.equals(prop.getAccessType())){
                p.add(prop);
            }
        }
        return p;
    }

    public String getFullyQualifiedNameJoinedBy(String separator) {
        if (namespaces != null && namespaces.length > 0){
            return StringUtil.join(namespaces, separator) + separator + name;
        }
        return name;
    }


    /**
     * TODO improve this, what properties can be persistable
     * @return
     */
    public List<ARIProp> getPersitablePrimitives(Context context) {
        List<ARIProp> p = new ArrayList<>();
        for (ARIProp prop: properties){
            if (!prop.isTranzient() && !(prop.isStatic() && prop.isFinal())  && prop.isPrimitive(context) ){
                p.add(prop);
            }
        }
        return p;
    }
}
