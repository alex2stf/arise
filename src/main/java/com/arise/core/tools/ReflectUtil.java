package com.arise.core.tools;



import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import static com.arise.core.tools.TypeUtil.isNull;



public class ReflectUtil {

    private static final Mole log = Mole.getInstance(ReflectUtil.class);




    public static boolean isGetter(Method method) {
        if (Modifier.isPublic(method.getModifiers()) &&
                method.getParameterTypes().length == 0) {
            if (method.getName().matches("^get[A-Z].*") &&
                    !method.getReturnType().equals(void.class))
                return true;
            if (method.getName().matches("^is[A-Z].*") &&
                    method.getReturnType().equals(boolean.class))
                return true;
        }
        return false;
    }


    public static boolean isSetter(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName().matches("^set[A-Z].*");
    }




    public static Constructor<?> getConstructor(String className, Class ... parameterTypes) {
        try {
            return Class.forName(className).getConstructor(parameterTypes);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static Object newInstance(Constructor<?> constructor, Object... parameters) {
        if (constructor == null) {
            return null;
        }

        try {
            return constructor.newInstance(parameters);
        }
        catch (Exception e) {
            return null;
        }
    }


    private static Class[] getTypes(Object ... args){
        if (args.length == 0){
            return null;
        }
        Class[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++){
            types[i] = args[i].getClass();
        }
        return types;
    }

    public static Object newInstance(String clazzName, Object ... args){
        Class[] types = getTypes(args);
        Constructor constructor = getConstructor(clazzName, types);
        if (constructor == null){
            return null;
        }
        return newInstance(constructor, args);
    }



    @Deprecated
    public static Method getMethod(String className, String methodName, Class<?>[] parameterTypes) {
        try
        {
            return Class.forName(className).getMethod(methodName, parameterTypes);
        }
        catch (Exception e)
        {
            return null;
        }
    }


//    public static Object invoke(Method method, Object object, Object... parameters) {
//        if (method == null)
//        {
//            return null;
//        }
//
//        try
//        {
//            return method.invoke(object, parameters);
//        }
//        catch (Exception e)
//        {
//            return null;
//        }
//    }

    public static boolean classExists(String classname){
        return getClassByName(classname) != null;
    }

    public static Class getClassByName(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean objectIsAssignableFrom(Object object, String classname){
        if (object == null || !classExists(classname)){
            return false;
        }
        return getClassByName(classname).isAssignableFrom(object.getClass());
    }



    public static boolean classNameExtends(String className, Class clazz){
        java.lang.Class thatClass = getClassByName(className);
        if (thatClass != null){
            return clazz.isAssignableFrom(thatClass);
        }
        return false;
    }

    public static Object createInterface(String interfaceName, IMethod... methods){
        return createInterface(new String[]{interfaceName}, methods);
    }

    public static Object createInterface(String[] interfacesNames, IMethod... methods){
        Set<Class> classList = new HashSet<Class>();
        for (String s: interfacesNames){
            Class c = getClassByName(s);
            if (c != null) {
                classList.add(getClassByName(s));
            } else {
                log.warn(s + " interface not found");
            }
        }
        Class[] classes = new Class[classList.size()];
        classes = classList.toArray(classes);
        return createInterface(classes, methods);
    }

    public static Object createInterface(Class[] interfaces, final IMethod... methods){
        ClassLoader cl = ReflectUtil.class.getClassLoader();
        return Proxy.newProxyInstance(cl, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) {
                for (IMethod m : methods){
                    if (m != null && m.getName().equals(method.getName())){
                        return m.getValue(objects);
                    }
                }
                return null;
            }
        });
    }


    public static InvokeHelper getMethodWithClassNames(Object object, String methodName, String... parameterTypes) {
        try {
            Class classes[] = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++){
                classes[i] = getClassByName(parameterTypes[i]);
            }
            return new InvokeHelper(object.getClass().getMethod(methodName, classes), object);
        } catch (NoSuchMethodException e) {
            return InvokeHelper.NULL;
        }
    }


    public static InvokeHelper getStaticMethod(String clazzName, String methodName, Class... parameterTypes) {
        try {
            Class clazz = getClassByName(clazzName);
            if (clazz == null){
                return InvokeHelper.NULL;
            }
            return new InvokeHelper(clazz.getMethod(methodName, parameterTypes), null);
        } catch (NoSuchMethodException e) {
            return InvokeHelper.NULL;
        }
    }


    public static InvokeHelper getStaticMethod(Class clazz, String methodName, Class... parameterTypes) {
        try {
            return new InvokeHelper(clazz.getMethod(methodName, parameterTypes), null);
        } catch (NoSuchMethodException e) {
            return InvokeHelper.NULL;
        }
    }

    public static InvokeHelper getMethod(Class clazz, Object object, String methodName, Class... parameterTypes) {

        if (object == null || clazz == null){
            return InvokeHelper.NULL;
        }
        try {
            return new InvokeHelper(clazz.getMethod(methodName, parameterTypes), object);
        } catch (NoSuchMethodException e) {
            return InvokeHelper.NULL;
        }
    }

    public static InvokeHelper findGetter(Object object, String name){
        InvokeHelper response = getMethod(object, name);

        if (response == InvokeHelper.NULL){
            String capped = StringUtil.capFirst(name);
            response = getMethod(object, "get" + capped);

            if (response == InvokeHelper.NULL){
                response = getMethod(object, "is" + capped);
            }
        }

        return response;
    }


    public static boolean hasAnyOfTheAnnotations(Field field, String ... names){
        return annotationsMatch(field.getAnnotations(), names) || annotationsMatch(field.getDeclaredAnnotations(), names);
    }

    public static boolean hasAnyOfTheAnnotations(Method field, String ... names){
        return annotationsMatch(field.getAnnotations(), names) || annotationsMatch(field.getDeclaredAnnotations(), names);
    }


    public static Object getAnnotation(Method method, String name){
        Class annotation = getClassByName(name);
        if (annotation != null){
            return method.getAnnotation(annotation);
        }
        return null;
    }

    public static int countParameters(Method method){
        InvokeHelper invoke = getMethod(method, "getParameterCount");
        if(invoke.getMethod() != null){
            Integer x = invoke.callForInteger();
            if (x != null){
                return x;
            }
        }
        return method.getParameterTypes().length;
    }

//    public static Object getAnnotation(Parameter parameter, String name){
//        Class annotation = getClassByName(name);
//        if (annotation != null){
//            return parameter.getAnnotation(annotation);
//        }
//        return null;
//    }

    public static Object getAnnotation(Class clazz, String name) {
        Class annotation = getClassByName(name);
        if (annotation != null){
            return clazz.getAnnotation(annotation);
        }
        return null;
    }

    public static boolean annotationsMatch(Annotation[] annotations, String [] names){
        for (Annotation a: annotations){
            for (String s: names){
                if (StringUtil.hasText(s) && (classMatchName(a.getClass(), s) || classMatchName(a.annotationType(), s)) ){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean classMatchName(Class t, String n){
        return !isNull(n) && !isNull(t) && (n.equals(t.getName()) || n.equals(t.getCanonicalName()) || n.equals(t.getSimpleName()));
    }




    public static InvokeHelper getMethod(String className, Object object, String methodName, Class... parameterTypes) {
        return getMethod(getClassByName(className), object, methodName, parameterTypes);
    }


    public static InvokeHelper getMethod(Object object, String methodName, Class... parameterTypes) {
        if (object == null){
            return InvokeHelper.NULL;
        }
        return getMethod(object.getClass(), object, methodName, parameterTypes);
    }

    public static Object readStaticMember(Class clazz, String name) {
        try {
            Field f = clazz.getField(name);
            return f.get(null);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static Integer getStaticIntProperty(Object o, String value) {
        if (o != null){
            try {
                return o.getClass().getField(value).getInt(null);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static boolean isInstanceOf(Object worker, String className) {
        if (isNull(worker)){
            return false;
        }

        if (className.equals(worker.getClass().getName())){
            return true;
        }

        if (className.equals(worker.getClass().getCanonicalName())){
            return true;
        }


        return objectIsAssignableFrom(worker, className);
    }


    public abstract static class IMethod<T>{

        private final String methodName;

        public IMethod(String methodName){
            this.methodName = methodName;
        }

        public final String getName(){
            return methodName;
        }

        public abstract T getValue(Object [] args);
    }

    public static class InvokeHelper{
        private Method method;
        private Object instance;

        public static final InvokeHelper NULL = new InvokeHelper();

        private InvokeHelper(){
            method = null;
            instance = null;
        }

        public static InvokeHelper of(Method m, Object i){
            return new InvokeHelper(m, i);
        }

        public Method getMethod() {
            return method;
        }

        public Object getInstance() {
            return instance;
        }

        public InvokeHelper(Method method, Object instance){
            this.method = method;
            this.instance = instance;
        }

        public <T> T callFor(Class<T> returnType, Object ... args){
            if (method == null){
                return null;
            }
            try {
               return (T) method.invoke(instance, args);
            } catch (Exception e) {
                return null;
            }
        }

        public boolean isAbstract(){
            if (method == null){
                return false;
            }
            return Modifier.isAbstract(method.getModifiers());
        }

        public boolean callForBoolean(Object ... args){
            return Boolean.TRUE.equals(callFor(Boolean.class, args));
        }

        public String callForString(Object ... args){
            return callFor(String.class, args);
        }

        public Object[] callForObjectList(Object ... args){
           try {
               return (Object[]) call(args);
           }catch (Exception e){
               return null;
           }
        }

        public String[] callForStringList(Object ... args){
            try {
                return (String[]) call(args);
            }catch (Exception e){
                return null;
            }
        }

        public Object call(Object ... args){
            if (method == null){
                return null;
            }
            try {
                return method.invoke(instance, args);
            } catch (IllegalAccessException e) {
                method.setAccessible(true);
                try {
                    return method.invoke(instance, args);
                } catch (Exception e1) {
                    return null;
                }
            } catch (InvocationTargetException e) {
                return null;
            }
        }

        public Integer callForInteger(Object ... args) {
            try {
                return (Integer) call(args);
            }catch (Exception e){
                return null;
            }
        }
    }
}
