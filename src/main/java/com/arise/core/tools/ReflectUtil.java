package com.arise.core.tools;


import com.arise.core.exceptions.DependencyException;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

//import java.lang.reflect.Parameter;


public class ReflectUtil {

    private static final Mole log = Mole.getInstance(ReflectUtil.class);

    public static synchronized URLClassLoader loadJars(List<File> jars){

        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < jars.size(); i++) {
            try {
                urls[i] = jars.get(i).toURI().toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        Note! If your loaded libraries uses some resources like properties or something else, you need to provide context class loader:


//        Thread.currentThread().setContextClassLoader(childClassLoader);
        return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    }

    public static synchronized void loadLibrary(java.io.File jar) throws Exception {
        /*We are using reflection here to circumvent encapsulation; addURL is not public*/
        ClassLoader loader = ClassLoader.getSystemClassLoader();

        //java.net.URLClassLoader
        java.net.URL url = jar.toURI().toURL();


        Method m = searchMethodInClass(loader.getClass(), "addURL", new Class[]{java.net.URL.class});
        m.setAccessible(true); /*promote the method to public access*/
        m.invoke(loader, new Object[]{url});
    }



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



    public static Method searchMethodInClass(Class clz, String mn, Class<?>[] pt) {
        Method m;
        try {
            m = clz.getMethod(mn, pt);
        } catch (NoSuchMethodException e) {
            try {
                m = clz.getDeclaredMethod(mn, pt);
            } catch (NoSuchMethodException ex) {
                log.trace("Cannot find with search method " + clz + "#" + mn);
                m = null;
            }
        }
        if (m == null && !clz.getSuperclass().equals(Object.class)){
            return searchMethodInClass(clz.getSuperclass(), mn, pt);
        }
        return m;
    }

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
        Class thatClass = getClassByName(className);
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
                ;;
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


//    public static InvokeHelper getMethodWithClassNames(Object object, String methodName, String... parameterTypes) {
//        try {
//            Class classes[] = new Class[parameterTypes.length];
//            for (int i = 0; i < parameterTypes.length; i++){
//                classes[i] = getClassByName(parameterTypes[i]);
//            }
//            return new InvokeHelper(object.getClass().getMethod(methodName, classes), object);
//        } catch (NoSuchMethodException e) {
//            return InvokeHelper.NULL;
//        }
//    }


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

    public static InvokeHelper getMethod(Class clz, Object o, String mn, Class... pt) {
        if (o == null || clz == null){
            return InvokeHelper.NULL;
        }
        Method m = searchMethodInClass(clz, mn, pt);
        if (m != null){
            return new InvokeHelper(m, o);
        }
        return InvokeHelper.NULL;
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
//            //org.springframework.core.annotation.AnnotationUtils.findAnnotation()
//            return parameter.getAnnotation(annotation);
//        }
//        return null;
//    }

    public static Object getAnnotation(Method xxx, String name){
        Class annotation = getClassByName(name);
        if (annotation != null){
            return findAnnotation(xxx, annotation);
        }
        return null;
    }

    public static Object getAnnotation(Class clazz, String name) {
        Class annotation = getClassByName(name);
        if (annotation != null){
            return findAnnotation(clazz, annotation);
        }
        return null;
    }

    public static <T> T findAnnotation(Class clazz, Class<T> annotation){
        T response = (T) clazz.getAnnotation(annotation);
        //search with spring
        if (response == null){
            Class annotationUtils = getClassByName("org.springframework.core.annotation.AnnotationUtils");
            if (annotationUtils != null){
               try {
                   response = (T) getStaticMethod(annotationUtils, "findAnnotation", Class.class, Class.class)
                           .call(clazz, annotation);
               } catch (Exception e){
                   e.printStackTrace();
               }

            }
        }
       return response;
    }

    public static <T extends Annotation> T findAnnotation(Method method, Class<T> annotation){
        T response = method.getAnnotation(annotation);
        //search with spring
        if (response == null){
            Class annotationUtils = getClassByName("org.springframework.core.annotation.AnnotationUtils");
            if (annotationUtils != null){
               try {
                   response = (T) getStaticMethod(annotationUtils, "findAnnotation", Method.class, Class.class)
                           .call(method, annotation);
               }
               catch (Exception e){
                   e.printStackTrace();
               }
            }
        }
        return response;
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
        return !TypeUtil.isNull(n) && !TypeUtil.isNull(t) && (n.equals(t.getName()) || n.equals(t.getCanonicalName()) || n.equals(t.getSimpleName()));
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

    public static Object getStaticObjectProperty(Object o, String value) {
        if (o != null){
            try {
                return o.getClass().getField(value).get(null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static boolean objectIsInstanceOf(Object worker, String className) {
        if (TypeUtil.isNull(worker)){
            return false;
        }
        return classIsInstanceOf(worker.getClass(), className);
    }


    public static boolean classIsInstanceOf(Class<?> clazz, String className) {
        if (clazz == null || !StringUtil.hasText(className)){
            return false;
        }
        Class testClazz = getClassByName(className);
        if (testClazz == null){
            return false;
        }
        return clazz.equals(testClazz)
                || testClazz.isAssignableFrom(clazz)
                || className.equals(clazz.getCanonicalName());
    }

    public static ClazzHelper getClass(String className, boolean initialize, URLClassLoader urlClassLoader) {
        try {
            return new ClazzHelper(Class.forName(className, initialize, urlClassLoader));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new ClazzHelper(null);
        }
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

    public static class ClazzHelper {
        private static final ClazzHelper NIL = new ClazzHelper(null);
        private Class clz;


        public Class getClazz(){
            return clz;
        }

        public ClazzHelper(Class clz) {
            this.clz = clz;
        }

        public InvokeHelper getStaticMethod(String name, Class... types){
            if(clz != null){
                try {
                    return new InvokeHelper(clz.getMethod(name, types), null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return InvokeHelper.NULL;
                }
            }

            return InvokeHelper.NULL;
        }

        public ConstructorHelper getConstructor(Class ... params) {
            if (clz != null){
                try {
                    return new ConstructorHelper(clz.getConstructor(params));
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConstructorHelper.NIL;
                }
            }
            return ConstructorHelper.NIL;
        }

        public ClazzHelper getNestedClass(String n) {
            if (clz != null){
                if (clz.getClasses() != null){
                    for (Class c: clz.getClasses()){
                        if (n.equalsIgnoreCase(c.getName())){
                            return new ClazzHelper(c);
                        }
                    }
                }
            }
            return ClazzHelper.NIL;
        }



        public Object getEnumValue(String v) {
            if (clz != null){
                for (Object o: clz.getEnumConstants()){
                    String n = getMethod(o, "name").callForString();
                    if (v.equals(n)){
                        return o;
                    }
                }
            }
            return null;
        }
    }


    public static class ConstructorHelper {
        public static final ConstructorHelper NIL = new ConstructorHelper(null);
        private final Constructor c;

        public ConstructorHelper(Constructor c) {
            this.c = c;
        }



        public Object newInstance(Object ... args) {
            if (c != null){
                try {
                    return c.newInstance(args);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
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
            //TODO optional setAccesible
            method.setAccessible(true);
            try {
                return method.invoke(instance, args);
            } catch (Exception e) {
                throw new DependencyException("Unable to invoke method " + method, e);
            }
        }

        public Integer callForInteger(Object ... args) {
            try {
                return (Integer) call(args);
            }catch (Exception e){
                return null;
            }
        }

        public Collection<Object> callForCollection(Object ... args) {
            return (Collection<Object>) call(args);
        }

        public Map callForMap(Object ... args) {
            return (Map) call(args);
        }
    }
}
