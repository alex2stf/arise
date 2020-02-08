package com.arise.cargo;

import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.models.FilterCriteria;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.TypeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arise.cargo.WebClientGenerator.ArgType.HEADER;
import static com.arise.cargo.WebClientGenerator.ArgType.PATH_VAR;
import static com.arise.cargo.WebClientGenerator.ArgType.QUERY_PARAM;
import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.TypeUtil.isDate;
import static com.arise.core.tools.TypeUtil.isPrimitive;
import static com.arise.core.tools.TypeUtil.isSingleKeyIterable;

public class WebClientGenerator {





    private MethodHandler methodHandler;



    private Map<String, List<JsMethod>> jsMethods = new HashMap<>();


    public WebClientGenerator setMethodHandler(MethodHandler methodHandler) {
        this.methodHandler = methodHandler;
        return this;
    }

    Map<Class, String> rootUris = new HashMap<>();

    public WebClientGenerator scanClass(Class clazz, String rootUri){
        final WebClientGenerator generator = this;
        rootUris.put(clazz, rootUri);
        TypeUtil.findAllMethods(clazz, new FilterCriteria<Method>() {
            @Override
            public boolean isAcceptable(Method data) {
                return methodHandler.handle(data, generator);
            }
        });
        return this;
    }

    public ContentMerger compile(Writer genWriter){

        ContentMerger writer = new ContentMerger(genWriter);
        Whisker whisker = new Whisker();


        for(Map.Entry<String, List<JsMethod>> entry: jsMethods.entrySet()){
            writer.write("function ", entry.getKey(), " (R) {\n");

            for (JsMethod method: entry.getValue()){
                InputStream inputStream = StreamUtil.readResource("/templates/web_client_gen.whiskey");
                method.rootVar = "R";
                try {
                    whisker.compile(new InputStreamReader(inputStream), genWriter, method);
                    writer.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.write("}\n");
        };


        return writer;
    }



    private String className(Class c){
        String s[] = c.getName().split("\\.");
        return s[s.length - 1];
    }

    public void registerMethod(Method m, String[] methods, String [] paths,
                               String[] produces, String[] consumes,
                               ParameterSolver parameterSolver, String nextCall){

        String className = className(m.getDeclaringClass());

        List<Argument> arguments = new ArrayList<>();

        for (Parameter parameter: m.getParameters()){
            Argument argument = parameterSolver.solve(parameter);
            if (argument != null){
                arguments.add(argument);
            }
        }

        String name = sanitize(m.getName());

        int mthi = 1;

        for (String mth: methods){
            mthi++;

            for (int i = 0; i < paths.length; i++){
                String actName = name + (i > 0 ? "P" + (i+1) : "");
                JsMethod method = new JsMethod();
                method.name = actName;
                method.consumes = consumes;
                method.produces = produces;
                method.httpMehod = mth.toUpperCase();
                method.path = paths[i];
                method.declaringClass = className(m.getDeclaringClass());
                method.nextCall = nextCall;
                if (rootUris.containsKey(m.getDeclaringClass())){
                    method.classRootUri = rootUris.get(m.getDeclaringClass());
                }
                method.setArguments(arguments);

                if (! jsMethods.containsKey(className)){
                    jsMethods.put(className, new ArrayList<JsMethod>());
                }
                jsMethods.get(className).add(method);

            }

            name+= "M" + mthi;
        }


    }


    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        compile(stringWriter);
        try {
            stringWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public String sanitize(String name){
        if ("get".toLowerCase().equalsIgnoreCase(name)){
            return "doGet";
        }
        if ("delete".toLowerCase().equalsIgnoreCase(name)){
            return "doDelete";
        }
        return name;
    }

    public WebClientGenerator scanMultipleClasses(ClassHandler classHandler, Class ... classes) {
        for (Class c: classes){
            scanClass(c, classHandler.computeRootUri(c));
        }
        return this;
    }


    enum ArgType {
        PATH_VAR, GENERIC_OBJECT, GENERIC_LIST, SCHEMA, HEADER, QUERY_PARAM;
    }


    public interface MethodHandler {
        boolean handle(Method method, WebClientGenerator generator);
    }

    public interface ClassHandler {
        String computeRootUri(Class clazz);
    }




    public interface ParameterSolver {
        Argument solve(Parameter parameter);
    }

    public static class Argument {

        ArgType type;
        String key;



        private Argument(){

        }

        public static Argument pathVariable(String name) {
            return ofType(name, PATH_VAR);
        }

        public static Argument queryParam(String name) {
            return ofType(name, QUERY_PARAM);
        }

        public static Argument header(String name) {
            return ofType(name, HEADER);
        }

        static Argument ofType(String name, ArgType type){
            Argument a = new Argument();
            a.type = HEADER;
            a.key = name;
            a.type = type;
            return a;
        }

        public static Argument bodyObject(Parameter parameter){
            return bodyObject(parameter, new FilterCriteria<Field>() {
                @Override
                public boolean isAcceptable(Field data) {
                    return !Modifier.isStatic(data.getModifiers()) && !data.getName().startsWith("this");
                }
            });
        }

        public static Argument bodyObject(Parameter parameter,  FilterCriteria<Field> schemaFilter) {
            Argument a = new Argument();
            a.key = parameter.getName();

            if (isPrimitive(parameter.getType()) || isDate(parameter.getType())){
                a.type = ArgType.GENERIC_OBJECT;
            }

            else if (isSingleKeyIterable(parameter.getType())){
                a.type = ArgType.GENERIC_LIST;
            }

            else {
                a.type = ArgType.SCHEMA;
                TypeUtil.findAllFields(parameter.getType(), schemaFilter);
            }
            return a;
        }

        @Override
        public String toString() {
            return "Argument{" +
                    "type=" + type +
                    ", key='" + key + '\'' +
                    ", name='" + getName() + '\'' +
                    '}';
        }

        public String getName() {
            return key.toLowerCase().replaceAll("-", "");
        }
    }

    public class JsMethod {
        public String[] consumes;
        public String[] produces;
        public String name;
        public String httpMehod;
        public String path;
        public String argumentsLine;
        public String pathLine;
        public String pathObject = "";
        public String headerObject = "";
        public String queryLine = "";
        public String declaringClass;
        Map<String, String> headersMap = new HashMap<>();
        Map<String, String> pathVarsMap = new HashMap<>();
        Map<String, String> queryMap = new HashMap<>();
        Set<String> paths = new HashSet<>();
        Set<String> bodies = new HashSet<>();
        public String nextCall;
        public String bodiesLine;
        String rootVar;
        String classRootUri = "";
        String acceptLine = "";
        String consumesLine = "";

        public void setArguments(List<Argument> arguments) {
            argumentsLine = StringUtil.join(arguments, ", ", new StringUtil.JoinIterator<Argument>() {
                @Override
                public String toString(Argument value) {
                    return value.getName();
                }
            });

            for (Argument argument: arguments){
                String argName = argument.getName();
                switch (argument.type){
                    case PATH_VAR:
                        pathVarsMap.put(argument.key, argName);
                        paths.add(argName);
                        break;
                    case HEADER:
                        headersMap.put(argument.key, argName);
                        break;
                    case QUERY_PARAM:
                        queryMap.put(argument.key, argName);
                        break;
                    default:
                        bodies.add(argName);

                }
            }

            pathObject = joinToString(pathVarsMap);
            headerObject = joinToString(headersMap);
            queryLine = joinToString(queryMap);
            pathLine = StringUtil.join(paths, ", ");
            bodiesLine = StringUtil.join(bodies, ", ");
            if (!isEmpty(consumes)){
                consumesLine = StringUtil.join(consumes, ", ");
            }

            if (!isEmpty(produces)){
                acceptLine = StringUtil.join(produces, ", ");
            }
        }

        private String joinToString(Map<String, String> map){
            if (isEmpty(map)){
                return "";
            }
            List<String> keys = new ArrayList<>();
            for (Map.Entry<String, String> entry: map.entrySet()){
                keys.add("\'"+entry.getKey()+"\' : " + entry.getValue());
            }
            return StringUtil.join(keys, ", ");
        }


    }



}
