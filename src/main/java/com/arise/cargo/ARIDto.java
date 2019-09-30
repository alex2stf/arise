package com.arise.cargo;

import com.arise.cargo.ARIMethod.Getter;
import com.arise.cargo.ARIMethod.Setter;
import com.arise.cargo.ARIMethod.Sourced;
import com.arise.core.tools.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ARIDto extends ARIType {


    private List<Full> fulls = new ArrayList<>();
    private Set<Prop> props = new HashSet<>();


    public ARIDto(String name, String[] namespaces) {
        super(name, namespaces, false);
    }

    public void addFull(String name, String[] excepts){
        Full f = new Full();
        f.excepts = excepts;
        f.name = name;
        fulls.add(f);
    }


    public void addProp(String thisName, String thatName, String thatType){
        Prop p = new Prop();
        p.thatName = thatName;
        p.thisName = thisName;
        p.thatType = thatType.trim();
        props.add(p);
    }



    public List<ARIType> getRequiredTypes(Context c){
        List<ARIType> r = new ArrayList<>();
        for (ARIProp p: getProperties(c)){
            ARIType t = p.getType(c);
            r.add(t);
        }
        for (Full f: fulls){
            ARIType t = c.getTypeByName(f.name);
            r.add(t);
        }

        for (Prop p: props){
            ARIType t = c.getTypeByName(p.thatType);
            r.add(t);
        }
        return r;
    }



    public List<ARIMethod> gettersAndSettersList(Context context){
        List<ARIMethod> r = new ArrayList<>();
        for (ARIProp prop: getProperties(context)){
            r.add(new Getter(prop, context));
            r.add(new Setter(prop, context));
        }

        return r;
    }


    public List<Sourced> getExtraSetters(Context context){
        List<Sourced> r = new ArrayList<>();



        for (Prop p: props){
            ARIType t = context.getTypeByName(p.thatType);
            ARIProp thatProp = t.getPropertyByName(p.thatName);
            ARIProp thisProp = ARIProp.cloneForDTO(p.thisName, thatProp);
            thisProp.setComments(Collections.<String>emptyList());
            thatProp.setLines(Collections.<String>emptyList());

            Sourced transferSetter = new Sourced(
                context.buildSetterName(thisProp.getName()),
                this.getName(),
                new String[]{StringUtil.lowFirst(p.thatType)},
                new String[]{ p.thatType },
                thatProp
            );
            r.add(transferSetter);
        }



        return r;
    }


    public Set<ARIType> getSources(Context context){
        Set<ARIType> r = new HashSet<>();
        for (Full f: fulls){
            ARIType x = context.getTypeByName(f.name);
            r.add(x);
        }
        return r;
    }


    public Set<ARIType> getBuilders(Context context){
        Set<ARIType> r = getSources(context);
        for (Prop p: props){
            ARIType x = context.getTypeByName(p.thatType);
            r.add(x);
        }
        return r;
    }

    public Set<ARIProp> getProperties(Context context){
        Set<ARIProp> res = new HashSet<>();
        res.addAll(convertLocalPropsToCRGProps(context));

        for (Full f: fulls){
            ARIType t = context.getTypeByName(f.name);
            for (ARIProp p: t.getProperties()){
                if (!isException(p, f)){

                    if (!hasPropertyNamed(res, p)){
                        ARIProp clone = ARIProp.cloneForDTO(p.getName(), p);
                        res.add(clone);
                    }
                }
            }
            if(t instanceof ARIClazz){
                ARIClazz thatClass = (ARIClazz) t;
                List<ARIProp> constructParameters = thatClass.getConstructParameters(context);


                if (constructParameters != null && !constructParameters.isEmpty()){
                    for (ARIProp cp: constructParameters){

                        if (!hasPropertyNamed(res, cp)){
                            ARIProp clone = ARIProp.cloneForDTO(cp.getName(), cp);
                            res.add(clone);
                        }
                    }
                }
            }
        }
        return res;
    }

    private boolean hasPropertyNamed(Set<ARIProp> set, ARIProp prop){
        for (ARIProp p: set){

            if (p.getName().equals(prop.getName())){
                return true;
            }
        }
        return false;
    }

    private Set<ARIProp> convertLocalPropsToCRGProps(Context context){
        Set<ARIProp> res = new HashSet<>();
        for (Prop p: props){
            ARIType t = context.getTypeByName(p.thatType);
            ARIProp thatProp = t.getPropertyByName(p.thatName);
            ARIProp thisProp = ARIProp.cloneForDTO(p.thisName, thatProp);
            thisProp.setComments(Collections.<String>emptyList());
            thatProp.setLines(Collections.<String>emptyList());
            res.add(thisProp);
        }
        return res;
    }




    private boolean isException(ARIProp prop, Full f){

        if (f.excepts != null && f.excepts.length > 0){
            for (String s: f.excepts){
                if (s.equals(prop.getName())){
                    return true;
                }
            }
        }
        return false;
    }

    public Getter getTransferGetter(Setter s, Context c) {
        //scan only full objects, since only this ones are considered sources of transfer
        for (Full f: fulls){
            //check if is not an exception of transfer
            for (String e: f.excepts){
                if (e.equals(s.source().getName())){
                    return null; // return null if no getter is created since property was an exception
                }
            }
        }
        return new Getter(s.source(), c);
    }

    public boolean typeIsSource(ARIType s) {
        for (Full f: fulls){
            if (f.name.equals(s.getName())){
                return true;
            }
        }
        return false;
    }

    public String getSetterNameOf(ARIType s, Context context) {
        for (Prop p: props){
            if (p.thatType.equals(s.getName())){
                return context.buildSetterName(p.thisName);
            }
        }
        return null;
    }

    private class Full {
        String name;
        String[] excepts;
    }

    private class Prop {
        String thisName;
        String thatName;
        String thatType;
    }
}
