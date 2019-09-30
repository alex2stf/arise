package com.arise.core.tools;

import static com.arise.core.tools.ReflectUtil.hasAnyOfTheAnnotations;
import static com.arise.core.tools.StringUtil.capFirst;

import com.arise.core.tools.ReflectUtil.InvokeHelper;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeUtil {


    /**
     * returns true if object is instance of {@link Boolean} and equals TRUE or is true
     * @param c
     * @return
     */
    public static boolean isBooleanTrue(Object c){
        if (c != null){
            if (c instanceof Boolean ){
                return Boolean.TRUE.equals(c);
            }
            if (c.getClass().equals(boolean.class)){
                return ((boolean)c) == true;
            }
        }
        return false;
    }

    /**
     * returns true is parameter is instance of {@link Iterable} but it's not a {@link Map}
     * @param o
     * @return
     */
    public static boolean isSingleKeyIterable(Object o) {
        return o != null && !(o instanceof Map) && (o instanceof Iterable || o.getClass().isArray());
    }

    private static final String[] jsonIgnoreAnnotations = new String[]{
            "com.arise.core.models.Transient",
            "javax.persistence.Transient",
            "java.beans.Transient",
            "com.fasterxml.jackson.annotation.JsonIgnore"
    };

    /**
     * to be used for extensions
     * @return
     */
    @SuppressWarnings("unused")
    public static final String[] getJsonIgnoreAnnotationsAnnotations(){
      return jsonIgnoreAnnotations;
    }

    public static void forEach(Object o, IteratorHandler iterator) {
      forEach(o, iterator, false, jsonIgnoreAnnotations);
    }


    public static void forEach(Object o, IteratorHandler iterator, boolean ignoreNulls) {
        forEach(o, iterator, ignoreNulls, jsonIgnoreAnnotations);
    }

    public static void forEach(Object o, IteratorHandler iterator,
                               boolean ignoreNulls, String[] skippablesAnnotations){
        if (o == null){
            return;
        }

        int i = 0;

        if (o.getClass().isArray()){
            for(int j = 0; j < Array.getLength(o); j++){
                Object val = Array.get(o, j);
                if (!ignoreNulls){
                    iterator.found(j, val, j);
                } else if (val != null){
                    iterator.found(i, val, i);
                    i++;
                }
            }
        }

        else if (o instanceof CharSequence){
            CharSequence s = (CharSequence) o;
            for (i = 0; i < s.length(); i++){
                iterator.found(s.charAt(i), s.charAt(i), i);
            }
        }

        else if (o instanceof Map) {
            Map<Object, Object> map = (Map) o;
            for (Map.Entry<Object, Object> entry: map.entrySet()) {
                Object val = entry.getValue();
                if (!ignoreNulls || val != null){
                    iterator.found(entry.getKey(),val, i);
                    i++;
                }
            }

        }
        else if (o instanceof Iterable) {
            for (Object x : ((Iterable) o)) {
                if (!ignoreNulls || x != null){
                    iterator.found(x, x, i);
                    i++;
                }
            }
        }

        else {
                List<Field> fields = findAllFields(o.getClass());
                Set<String> usedNames = new HashSet<>();

                for (Field field: fields){
                    //skip references to self
                    if (Modifier.isFinal(field.getModifiers()) && field.getName().startsWith("this$")){
                        continue;
                    }
                    String name = field.getName();
                    Object value = null;
                    boolean skippable = true;

                    //first scan for getter:
                    InvokeHelper helper = ReflectUtil.findGetter(o, field.getName());
                    if (helper != InvokeHelper.NULL && !hasAnyOfTheAnnotations(helper.getMethod(), skippablesAnnotations)){
                        value = helper.call();
                        skippable = false;
                        usedNames.add(helper.getMethod().getName());
                    }
                    //skip private fields
                    else if(!Modifier.isPrivate(field.getModifiers()) && !hasAnyOfTheAnnotations(field, skippablesAnnotations)){

                        try {
                            field.setAccessible(true);
                            value = field.get(o);
                        } catch (Exception e) {
                            e.printStackTrace();
                            value = null;
                        }
                        skippable = false;
                    }

                    if (!skippable  && (!ignoreNulls || value != null)){
                        iterator.found(name, value, i);
                        i++;
                        usedNames.add("get" + capFirst(name));
                    }

                }

                List<Method> getters = findAllMethods(o.getClass(), new FilterCriteria<Method>() {
                    @Override
                    public boolean isAcceptable(Method m) {
                        return isGetter(m) && !hasAnyOfTheAnnotations(m, skippablesAnnotations) && !usedNames.contains(m.getName());
                    }
                });

                for (Method m: getters){
                    InvokeHelper helper = InvokeHelper.of(m, o);
                    iterator.found(StringUtil.lowFirst(m.getName().substring(3)), helper.call(), i);
                }
        }
    }



    private static boolean isGetter(Method m){
        boolean cc = m.getName().startsWith("get") && m.getParameterCount() == 0 && !"getClass".equals(m.getName());
        boolean bb = m.getName().startsWith("is") && m.getParameterCount() == 0 && ( Boolean.class.equals(m.getReturnType()) || boolean.class.equals(m.getReturnType())) ;
        return cc || bb;
    }


    public static List<Field> findAllFields(final Class<?> cls) {
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }


    public static List<Method> findAllMethods(final Class<?> cls, FilterCriteria<Method> filter) {
        final List<Method> allMethods = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Method[] declaredMethods = currentClass.getDeclaredMethods();
            for (Method m: declaredMethods){
                if (filter.isAcceptable(m)){
                    allMethods.add(m);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return allMethods;
    }




    /**
     * json specific.move to groot
     * @param in
     * @return
     */
    @Deprecated
    public static boolean isJsonTrueSequence(byte [] in){
        return in.length == "true".length() &&
            (in[0] == 't') &&
            (in[1] == 'r') &&
            (in[2] == 'u') &&
            (in[3] == 'e');
    }


    /**
     * json specific.move to groot
     * @param in
     * @return
     */
    @Deprecated
    public static boolean isJsonFalseSequence(byte [] in){
        return in.length == "false".length() &&
            (in[0] == 'f') &&
            (in[1] == 'a') &&
            (in[2] == 'l') &&
            (in[3] == 's') &&
            (in[4] == 'e');
    }

    /**
     * returns true if the object is null, is false or is an empty list
     * @param c
     * @return
     */
    public static boolean invert(Object c){
        if (c == null){
            return true;
        }
        if (isBoolean(c) && !isBooleanTrue(c)){
            return false;
        }
        if (c instanceof Collection){
            return ((Collection)c).isEmpty();
        }
        if (c.getClass().isArray()){
            return Array.getLength(c) == 0;
        }
        return false;
    }

    public static boolean isNumericSequence(CharSequence in){
        if (in == null || in.length() == 0 ){
            return false;
        }
        if (in instanceof String){
            String s = (String) in;
            return isNumericSequence(s.getBytes(), 0, s.getBytes().length);
        }

        byte[] buff = new byte[in.length()];
        for(int i = 0; i < in.length(); i++) {
           buff[i] = (byte) in.charAt(i);
        }
        return isNumericSequence(buff, 0, buff.length);
    }


    public static boolean isNumericSequence(byte [] in, int from, int to){
        return isNumericSequence(in, from, to, '.', '-');
    }


    public static boolean isNumericSequence(byte [] in, int from, int to, char d, char n){
        if (in == null || in.length == 0 || from >= to){
            return false;
        }
        if (in[0] == n){
            from += 1;
        }
        int qts = 0;
        for(int i = from; i < to; i++) {
            //TODO arg
            if (in[i] == d){
                qts++;
                if (qts > 1){
                    return false;
                }
            }
            else if(Character.digit(in[i],10) < 0) {
                return false;
            }
        }

        return true;
    }

    public static Double toDouble(byte [] in, int from, int to){
        return toDouble(in, from, to, '.', '-');
    }

    public static Number toNumber(byte [] in, int from, int to){
        return toNumber(in, from, to, '.', '-');
    }

    public static Number toNumber(byte [] in, int from, int to,  char d, char n){
        Double r = toDouble(in, from, to, d, n);
        if (r == null){
            return null;
        }
        if (isIntegerRange(r)){
            return r.intValue();
        }
        return r;
    }



    public static Double toDouble(byte [] in, int from, int to, char d, char n){
        if (in == null || in.length == 0 || from >=to ){
            return null;
        }
        boolean isNg = false;

        if (in[0] == n){
            from += 1;
            isNg = true;
        }

        int size = to - from;
        int vls[] = new int[size];
        int qts = 0;

        int cnt = 0;
        double fun = 0;
        double dun = 0;
        int vlsize = vls.length;

        for(int i = from; i < to; i++) {
            if (in[i] == d){
                qts++;
                if (qts > 1) { //2 delimiters are not allowed
                    return null;
                }
                vlsize-=1; //skip last value since it will be 0
                continue;
            }

            int intVal = Character.digit(in[i],10); //must always be positive
            if (intVal < 0){
                return null;
            }
            else {
                if (qts > 0){
                    dun++;
                }
                fun++;

                vls[cnt] = intVal;
            }
            cnt++;


        }

        fun = Math.pow(10, fun - 1); //calulate number of zeros
        if (dun > 0){
            dun = Math.pow(10, dun);
        }


        double response = 0;
        for(int i = 0; i < vlsize; i++){
            response += fun * vls[i];
            fun = fun / 10;
        }

        if (dun > 0){
            response = response / dun;
        }

        if (isNg){
            response = response * -1;
        }


        return response;
    }

    public static boolean isNull(Object value){
        return value == null;
    }


    public static boolean isBoolean(Object o){
        return testClasses(o, Boolean.class, boolean.class);
    }

    public static boolean isInteger(Object o) {
        return testClasses(Integer.class, int.class);
    }


    public static boolean isNumber(Object o){
        return testClasses(o, Number.class, int.class, float.class, double.class, long.class, Integer.class, Float.class, Double.class, Long.class);
    }


    private static boolean testClasses(Object o, Class<?> ... cls){
        if (isNull(o)){
            return false;
        }
        for (Class t: cls){
            if (o.getClass().equals(t) || o.getClass().isAssignableFrom(t)){
                return true;
            }
        }
        return false;
    }




    //TODO improve
    public static boolean isIntegerRange(Object o) {
        if (isNull(o)){
            return false;
        }

        if (isInteger(o)){
            return true;
        }
        if (o instanceof Double ){
            Double d = (Double) o;
            return !Double.isInfinite(d) && d == Math.floor(d);
        }

        if ((o instanceof Integer) || o.getClass().isAssignableFrom(int.class)){
            return true;
        }

        return false;
    }

    public static boolean isLong(Object o) {
        return testClasses(o, long.class, Long.class);
    }

    public interface IteratorHandler {
        void found(Object key, Object value, int index);
    }

    public static boolean isPrimitive(Object o) {
        return o instanceof String || isNumber(o);
    }

    public static Object getField(String name, Object obj){
        if (obj == null || !StringUtil.hasText(name)){
            return null;
        }

        if (obj instanceof Map && ((Map)obj).containsKey(name)){
            return ((Map) obj).get(name);
        }

        try {
            Field field = obj.getClass().getDeclaredField(name);
            if (!Modifier.isPrivate(field.getModifiers())){
                field.setAccessible(true);
            }
            field.getModifiers();
            return field.get(obj);
        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
//            e.printStackTrace();
            return null;
        }
    }
}
