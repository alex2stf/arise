package com.arise.core.tools;


import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

public class TypeUtil {


    /**
     * returns true if object is instance of {@link Boolean} and equals TRUE or is true
     * retuns true is object is instance of {@link String} and {@link StringUtil#hasText(String)}
     * @param c
     * @return
     */
    public static boolean isBooleanTrue(Object c){
        if (c != null){
            if (c instanceof String){
                return StringUtil.hasText((String) c);
            }
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
        return o != null && !(o instanceof Map) &&
                (testClasses(o, Iterable.class, Set.class, List.class) || o.getClass().isArray());
    }

    private static final String[] ignorableAnnotations = new String[]{
            "com.arise.core.models.Transient",
            "javax.persistence.Transient",
            "java.beans.Transient",
    };

    public static final String[] jsonIgnoreAnnotations = new String[]{
            ignorableAnnotations[0],
            ignorableAnnotations[1],
            ignorableAnnotations[2],
            "com.fasterxml.jackson.annotation.JsonIgnore"
    };






    public static final FilterCriteria<Method> getterMethodFiltering = new FilterCriteria<Method>() {
        @Override
        public boolean isAcceptable(Method m) {
            return m != null && !Modifier.isStatic(m.getModifiers())
                    && isGetter(m)
                    && !ReflectUtil.hasAnyOfTheAnnotations(m, jsonIgnoreAnnotations);
        }
    };

    public static final FilterCriteria<Field> defaultFieldFiltering = new FilterCriteria<Field>() {
        @Override
        public boolean isAcceptable(Field field) {
            return field != null && !Modifier.isFinal(field.getModifiers())
                    && !field.getName().startsWith("this")
                    && !Modifier.isStatic(field.getModifiers())
                    && !ReflectUtil.hasAnyOfTheAnnotations(field, jsonIgnoreAnnotations);
        }
    };




    public static void forEach(Object o, IteratorHandler iterator) {
      forEach(o, iterator, false, getterMethodFiltering, defaultFieldFiltering);
    }


    public static void forEach(Object o, IteratorHandler iterator, boolean ignoreNulls) {
        forEach(o, iterator, ignoreNulls, getterMethodFiltering, defaultFieldFiltering);
    }

    public static void forEach(Object o, IteratorHandler iterator,
                               boolean ignoreNulls,
                               final FilterCriteria<Method> methodFilterCriteria,
                               FilterCriteria<Field> fieldFilterCriteria){
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
                List<Field> fields = findAllFields(o.getClass(), fieldFilterCriteria);
                final Set<String> usedNames = new HashSet<>();

                for (Field field: fields){
                    //skip references to self
                    if (field.getType().equals(java.lang.reflect.Constructor.class)){
                        continue;
                    }

                    String name = field.getName();
                    Object value = null;
                    boolean skippable = true;

                    //first searchIcons for getter:
                    ReflectUtil.InvokeHelper helper = ReflectUtil.findGetter(o, field.getName());
                    if (helper != ReflectUtil.InvokeHelper.NULL && helper.getMethod() != null && methodFilterCriteria.isAcceptable(helper.getMethod())){
                        value = helper.call();
                        skippable = false;
                        usedNames.add(helper.getMethod().getName());
                    }

                    //skip private fields
                    else {
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
                        usedNames.add("get" + StringUtil.capFirst(name));
                    }

                }

                List<Method> getters = findAllMethods(o.getClass(), new FilterCriteria<Method>() {
                    @Override
                    public boolean isAcceptable(Method m) {
                        return methodFilterCriteria.isAcceptable(m) && !usedNames.contains(m.getName());
                    }
                });
                for (Method m: getters){
                    ReflectUtil.InvokeHelper helper = ReflectUtil.InvokeHelper.of(m, o);
                    iterator.found(StringUtil.lowFirst(m.getName().substring(3)), helper.call(), i);
                }
        }
    }



    private static boolean isGetter(Method m){
        int cpp = countMethodParameters(m);
        boolean cc = m.getName().startsWith("get") && cpp == 0 && !"getClass".equals(m.getName());
        boolean bb = m.getName().startsWith("is") && cpp == 0 && ( Boolean.class.equals(m.getReturnType()) || boolean.class.equals(m.getReturnType())) ;
        return cc || bb;
    }

    private static int countMethodParameters(Method m){
        Integer count = ReflectUtil.getMethod(m, "getParameterCount").callForInteger();
        if (count == null){
            Object params[] = ReflectUtil.getMethod(m, "getParameters").callForObjectList();
            if (params != null){
                count = params.length;
            } else {
                params = ReflectUtil.getMethod(m, "getParameterTypes").callForObjectList();
                if (params != null){
                    count = params.length;
                }
            }
        }
        return count != null ? count : 0;
    }


    public static List<Field> findAllFields(final Class<?> cls,  FilterCriteria<Field> filter) {
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field f: declaredFields){
                if (filter.isAcceptable(f)){
                    allFields.add(f);
                }
            }
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
     * returns true if the object is null, is false or is an empty list
     * @param c
     * @return
     */
    public static boolean invert(Object c){
        if (TypeUtil.isNull(c)){
            return true;
        }
        if (c instanceof String){
            return !StringUtil.hasContent((String) c);
        }

        if (isBoolean(c) && !isBooleanTrue(c)){
            return true;
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
        return testClasses(o, int.class, float.class, double.class, long.class,
                Number.class, Integer.class, Float.class, Double.class, Long.class, BigDecimal.class);
    }


    private static boolean testClasses(Object o, Class<?> ... cls){
        if (isNull(o)){
            return false;
        }
        for (Class t: cls){
            if (o == t || o.equals(t) || o.getClass().equals(t) || t.isAssignableFrom(o.getClass())){
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

    public interface IteratorConverter {
        IteratorConverter VALUE = new IteratorConverter() {
            @Override
            public Object convert(Object key, Object value, int index) {
                return value;
            }
        };

        Object convert(Object key, Object value, int index);
    }

    public static boolean isPrimitive(Object o) {
        return isNull(o) || o instanceof String || isNumber(o) || isBoolean(o) || o.equals(String.class)
                || o.equals(Object.class);
    }


    public static Object getField(final String name, Object obj,
                                  final FilterCriteria<Field> fieldFilterCriteria,
                                  final FilterCriteria<Method> methodFilterCriteria){
        if (isNull(obj) || !StringUtil.hasText(name)){
            return null;
        }

        if (CollectionUtil.mapContains(obj, name)){
            return ((Map) obj).get(name);
        }

        Object response = null;
        List<Field> fields = findAllFields(obj.getClass(), new FilterCriteria<Field>() {
            @Override
            public boolean isAcceptable(Field data) {
                return fieldFilterCriteria.isAcceptable(data) && name.equals(data.getName());
            }
        });
        if (!CollectionUtil.isEmpty(fields)){
            Field field = fields.get(0);
            field.setAccessible(true);
            try {
                response = field.get(obj);
                return response;
            } catch (IllegalAccessException e) {
                response = null;
            }
        }

        List<Method> methods = findAllMethods(obj.getClass(), new FilterCriteria<Method>() {
            @Override
            public boolean isAcceptable(Method m) {
                boolean isAcceptable = methodFilterCriteria.isAcceptable(m);
                boolean isGetter1 = ("get" + StringUtil.capFirst(name)).equals(m.getName());
                boolean isGetter2 = ("is" + StringUtil.capFirst(name)).equals(m.getName());
                boolean isInlineMethod = name.equals(m.getName());
                boolean hasNoArgs = ReflectUtil.countParameters(m) == 0;
                return methodFilterCriteria.isAcceptable(m) && hasNoArgs && (isGetter1 || isGetter2 || isInlineMethod);
            }
        });


        if (!CollectionUtil.isEmpty(methods)){
            Method m = methods.get(0);
            m.setAccessible(true);
            try {
                response = m.invoke(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }


        if (response == null){
//            throw new RuntimeException("Unable to find field " + name + " inside class" + obj.getClass());
        }







        return response;
    }


    public static Object search(String[] keys, Object o, int i,
                                FilterCriteria<Field> fieldFilterCriteria, FilterCriteria<Method> methodFilterCriteria){
        String ckey = keys[i];
        Object next = getField(ckey, o, fieldFilterCriteria, methodFilterCriteria);
        if (next != null && i < keys.length -1){
            return search(keys, next, i+1, fieldFilterCriteria, methodFilterCriteria);
        }
        return next;
    }

    public static Object search(String []keys, Object o, int index) {
        return search(keys, o, index, defaultFieldFiltering, getterMethodFiltering);
    }

    public static Object search(String []keys, Object o) {
        return search(keys, o, 0, defaultFieldFiltering, getterMethodFiltering);
    }


    public static Map<String, Object> objectToMap(Object s){
        return objectToMap(s, false, IteratorConverter.VALUE, defaultFieldFiltering, getterMethodFiltering);
    }

    public static boolean testClassesByReflection(Object o, String ... classNames){
        List<Class> classesToCheck = new ArrayList<>();
        for (String s: classNames){
            Class t = ReflectUtil.getClassByName(s);
            if (t != null){
                classesToCheck.add(t);
            }
        }
        Class [] items = new Class[classesToCheck.size()];
        classesToCheck.toArray(items);
        return testClasses(o, items);
    }

    public static boolean isDate(Object o){
        return testClassesByReflection(o, "sun.util.calendar.Gregorian$Date", "java.util.Date");
    }

    public static <T> Map<String, Object> objectToMap(Object s, final boolean ignoreNulls,
                                                      final IteratorConverter converter,
                                                      final FilterCriteria<Field> fieldFilterCriteria,
                                                      final FilterCriteria<Method> methodFilterCriteria){
        final Map<String, Object> response = new HashMap<>();
        final Set<Object> buffer = new HashSet<>();
        buffer.add(s);
        forEach(s, new IteratorHandler() {
            @Override
            public void found(Object key, Object value, int index) {
                if (isPrimitive(value) || isDate(value)){
                    response.put(String.valueOf(key), converter.convert(key, value, index));
                }
                else if(!buffer.contains(value)) {
                    Object inner = objectToMap(value, ignoreNulls, converter, fieldFilterCriteria, methodFilterCriteria);
                    response.put(String.valueOf(key), inner);
                    buffer.add(value);
                }
            }
        }, ignoreNulls, methodFilterCriteria, fieldFilterCriteria);
        return response;
    }



    public static void setField(String name, Object object, Object value){
        Field field = null;
        try {
            field = object.getClass().getDeclaredField(String.valueOf(name));
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            field = null;
        }

        if (field == null){
            ReflectUtil.getMethod(object, "set" + StringUtil.capFirst(name), value.getClass())
                    .call(value);
        }
    }


    public static Type getLastRawType(Type t){
        if (t instanceof ParameterizedType){
            return getLastRawType(((ParameterizedType)t).getRawType());
        }
        return t;
    }
}
