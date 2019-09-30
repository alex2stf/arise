package com.arise.cargo;

import com.arise.core.exceptions.SyntaxException;
import com.arise.cargo.ARIMethod.Getter;
import com.arise.cargo.ARIMethod.Setter;
import com.arise.core.tools.StringUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ARIClazz extends ARIType {
    private final boolean persistable;
    private final boolean embeddable;
    private final ClazzMode clazzMode;
    private final boolean xfinal;
    private final boolean xabstract;
    private final String[] implementedTypes;
    private final String[] extendNames;
    private final AccessType access;
    private final String alias;
    private final String schema;
    public ARIClazz(
        String name,
        String[] namespaces,
        boolean persistable,
        boolean embeddable,
        ClazzMode clazzMode,
        boolean xfinal,
        boolean xabstract,
        AccessType accessType,
        String[] extend,
        String[] implementedTypes,
        String alias,
        String schema,
        boolean iterable,
        String[] diamondTypes){

        super(name, namespaces, iterable, diamondTypes);
        this.persistable = persistable;
        this.clazzMode = clazzMode;
        this.xfinal = xfinal;
        this.xabstract = xabstract;
        this.alias = alias;
        this.implementedTypes = implementedTypes;
        this.extendNames = extend;
        this.access = accessType;
        this.schema = schema;
        this.embeddable = embeddable;
    }

    public static ARIClazz newTable(String schema, String name) {
        return new ARIClazz(
            (schema != null ? schema + "." + name : name),
            (schema != null) ? new String[]{schema, name} : null,
            true, false,
            ClazzMode.STANDARD,true,false, AccessType.PUBLIC, null,null, name, schema,false,null);
    }

    public static ARIClazz newTable(String name) {
        return newTable(null, name);
    }

    public boolean isEmbeddable() {
        return embeddable;
    }

    public List<ARIType> getExtendedTypes(Context context) {
        List<ARIType> res = new ArrayList<>();
        for (String s: extendNames){
            res.add(context.getTypeByName(s));
        }
        return res;
    }

    @Override
    public void addProperty(ARIProp p) {
        super.addProperty(p);
    }

    public boolean hasExtends(){
        return extendNames != null && extendNames.length > 0;
    }

    public AccessType getAccess() {
        return access;
    }



    public List<ARIType> getImplementedTypes(Context context) {
        List<ARIType> res = new ArrayList<>();
        for (String s: implementedTypes){
            res.add(context.getTypeByName(s));
        }
        return res;
    }

    public boolean isAbstract() {
        return xabstract;
    }

    public boolean isPersistable() {
        return persistable;
    }

    public ClazzMode getClazzMode() {
        return clazzMode;
    }

    public boolean isFinal() {
        return xfinal;
    }

    public boolean hasImplements(){
        return implementedTypes != null && implementedTypes.length > 0;
    }




    public String getSchema(){
        return schema;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        if (extendNames != null){
            sb.append(" ! " + StringUtil.join(extendNames, ","));
        }

        if (implementedTypes != null){
            sb.append(" ? " + StringUtil.join(implementedTypes, ","));
        }
        return sb.toString();
    }

    public boolean isInteraface() {
        return clazzMode.equals(ClazzMode.INTERFACE);
    }


    public List<ARIProp> getConstructParameters(Context context){
        List<ARIProp> finals = getFinalNonStaticProperties();
        for (ARIProp prop: getInheritedFinals(context)){
            finals.add(prop);
        }
        return finals;
    }


    public List<ARIProp> getInheritedFinals(Context context) {
        List<ARIProp> finals = new ArrayList<>();
        if (hasExtends()){
            for (ARIType ctype: getExtendedTypes(context)){
                if (ctype instanceof ARIClazz && ctype.hasNonStaticFinals()){
                    ARIClazz cClass = (ARIClazz) ctype;
                    for (ARIProp cProp: cClass.getConstructParameters(context)){
                        finals.add(cProp);
                    }
                }
            }
        }
        return finals;
    }

    public List<ARIType> getPropertiesTypes(Context context) {
        List<ARIType> r = new ArrayList<>();
        if (this.getProperties().isEmpty()){
            return r;
        }
        for (ARIProp prop: getProperties()){
            r.add(context.getTypeByName(prop.getTypeName()));
        }
        return r;
    }

    @SuppressWarnings("unused")
    public ARIType getTypeForDiamond(ARIGeneric c, Context context) {
        List<ARIType> list = getExtendedTypes(context);
        if (list.size() > c.getIndex()) {
            return list.get(c.getIndex());
        }

        list = getImplementedTypes(context);
        if (list.size() > c.getIndex()) {
            return list.get(c.getIndex());
        }
        throw new SyntaxException("No type found for diamond" + c);
    }

    public String namespaceId(){
        if (namespaces != null){
            return StringUtil.join(namespaces, ".");
        }
        return "";
    }


    public boolean hasPropBasedMethods(Context context) {
        return !getterList(context).isEmpty() && !setterList(context).isEmpty();
    }

    public List<Getter> getterList(Context context){
        List<ARIMethod.Getter> r = new ArrayList<>();
        for (ARIProp prop: properties){
            if (prop.allowGet()){
                r.add(new Getter(prop, context));
            }
        }
        return r;
    }


    public List<Setter> setterList(Context context){
        List<Setter> r = new ArrayList<>();
        for (ARIProp prop: properties){
            if (prop.allowSet() ){
                r.add(new Setter(prop, context));
            }

        }
        return r;
    }




    public List<ARIMethod> gettersAndSettersList(Context context){
        if (properties.isEmpty()){
            return Collections.emptyList();
        }
        Collections.sort(properties);
        List<ARIMethod> r = new ArrayList<>();
        for (ARIProp prop: properties){
            if (prop.allowGet()){
                r.add(new Getter(prop, context));
            }

            if (prop.allowSet()){
                r.add(new Setter(prop, context));
            }
        }

        return r;
    }

    public Collection<ARIType> getRequiredTypes(Context context) {
        Map<String, ARIType> res = new HashMap<>();
        fill(res, getExtendedTypes(context));
        fill(res, getImplementedTypes(context));
        fill(res, getPropertiesTypes(context));
        return res.values();
    }


    private void fill(Map<String, ARIType> res, Collection<ARIType> from){
        for (ARIType t: from) {
            if (!res.containsKey(t.getId())){
                res.put(t.getId(), t);
            }

            if (t.hasDiamonds()){
                for (ARIType dimpl: t.getCurrentDiamondImplementations()){
                    if (!res.containsKey(dimpl.getId())){
                        res.put(dimpl.getId(), dimpl);
                    }
                }
            }
        }
    }


    public boolean needsConstructor(Context context){
        return !getFinalNonStaticProperties().isEmpty() || !getInheritedFinals(context).isEmpty();
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public ARIProp addColumn(String name, PrimitiveType type) {
        return addColumn(name, type, false, false, false);
    }

    public ARIProp addColumn(String name, PrimitiveType type, boolean notnull, boolean primaryKey, boolean unique) {
        return addColumn(name, type, notnull, primaryKey, unique, null);
    }

    public ARIProp addColumn(String name, PrimitiveType type, boolean notnull, boolean primaryKey, boolean unique, Integer length) {
       for (ARIProp p: properties){
           if (name.equals(p.getAlias())){
               throw new IllegalArgumentException("Column " + name + " already registered as property " + p);
           }
       }

       ARIProp prop = new ARIProp.Builder(this, name)
           .setAlias(name).setTypeName(type.name().toLowerCase())
           .setIsPrimaryKey(primaryKey).setNullable(!notnull)
           .setUnique(unique)
           .setMaxLength(length)
           .build();
       addProperty(prop);

        return prop;
    }


    public boolean hasPropertyName(String name) {
        for (ARIProp p: properties){
            if (String.valueOf(name).equals(p.getName())){
                return true;
            }
        }
        return false;
    }
}
