package com.arise.cargo.impl;

import com.arise.cargo.WebClientGenerator;
import com.arise.core.tools.ReflectUtil;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.ReflectUtil.getAnnotation;
import static com.arise.core.tools.ReflectUtil.getMethod;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.TypeUtil.isPrimitive;


public class SpringWebClientHandler implements WebClientGenerator.MethodHandler,
        WebClientGenerator.ParameterSolver,
        WebClientGenerator.ClassHandler {

    private String []  search(String [] in, String [] in2){
        Set<String> mtSet = new HashSet<>();

        if (!isEmpty(in)){
            for (String s: in){
                if (hasText(s)){
                    mtSet.add(s);
                }
            }
        }

        if (!isEmpty(in2)){
            for (String s: in2){
                if (hasText(s)){
                    mtSet.add(s);
                }
            }
        }

        String [] res = new String[mtSet.size()];
        mtSet.toArray(res);
        return res;
    }

    private String[] findPaths(Object sprinAnn){
        return search(
                getMethod(sprinAnn, "path").callForStringList(),
                getMethod(sprinAnn, "value").callForStringList()
        );
    }

    private String getTypeName(Parameter parameter){
        String[] ll = String.valueOf(parameter.getType()).split("\\.");
        String name =  ll[ll.length-1].toLowerCase();
        if (isPrimitive(parameter.getType()) && name.length() > 3){
            name = name.substring(0, 3);
        }
        return name;
    }

    private String searchNV(Object o, Parameter parameter){
        String pathName = getMethod(o, "value").callForString();
        if (!hasText(pathName)){
            pathName = getMethod(o, "name").callForString();
        }
        if (!hasText(pathName)){
            pathName = getTypeName(parameter) + parameter.getName();
        }
        return pathName;
    }


    protected String getNextCall(Method method){
        return "_x_http_request";
    }


    @Override
    public boolean handle(Method method, WebClientGenerator gen) {
        String nextCall = getNextCall(method);
        Object annotation = getAnnotation(method, "org.springframework.web.bind.annotation.GetMapping");
        if (annotation != null){
            return handleAnnotation(new String[] {"get"}, annotation, method, gen, nextCall);
        }

        annotation= getAnnotation(method, "org.springframework.web.bind.annotation.PutMapping");
        if (annotation != null){
            return handleAnnotation(new String[] {"put"}, annotation, method, gen, nextCall);
        }

        annotation= getAnnotation(method, "org.springframework.web.bind.annotation.PostMapping");
        if (annotation != null){
            return handleAnnotation(new String[] {"post"}, annotation, method, gen, nextCall);
        }

        annotation = getAnnotation(method, "org.springframework.web.bind.annotation.DeleteMapping");
        if (annotation != null){
            return handleAnnotation(new String[] {"delete"}, annotation, method, gen, nextCall);
        }

        annotation = getAnnotation(method, "org.springframework.web.bind.annotation.PatchMapping");
        if (annotation != null){
            return handleAnnotation(new String[] {"patch"}, annotation, method, gen, nextCall);
        }

        annotation = getAnnotation(method, "org.springframework.web.bind.annotation.RequestMapping");

        if (annotation != null){
            String[] rqmths = new String[]{};
            Object[] methods = getMethod(annotation, "method").callForObjectList();
            if (!isEmpty(methods)){
                rqmths = new String[methods.length];
                for (int i = 0; i < rqmths.length; i++){
                    rqmths[i] = getMethod(methods[i], "name").callForString();
                }
            }
            String[] methodNames = search(
                    new String[]{ getMethod(annotation, "name").callForString()},
                    rqmths
            );
            return handleAnnotation(methodNames, annotation, method, gen, nextCall);
        }



        return false;

    }

    protected boolean handleAnnotation(String[] methodNames, Object annotation, Method method, WebClientGenerator gen, String nextCall){
        String[] consumes = getMethod(annotation, "consumes").callForStringList();
        String[] produces = getMethod(annotation, "produces").callForStringList();
        String[] paths = findPaths(annotation);
        gen.registerMethod(method, methodNames, paths, consumes, produces,
                this, nextCall);
        return true;
    }


    @Override
    public WebClientGenerator.Argument solve(Parameter parameter) {
        Object annotation = getAnnotation(parameter, "org.springframework.web.bind.annotation.PathVariable");
        if (annotation != null){
            String name = searchNV(annotation, parameter);
            return WebClientGenerator.Argument.pathVariable(name);
        }

        annotation = getAnnotation(parameter, "org.springframework.web.bind.annotation.RequestBody");
        if (annotation != null){
            return WebClientGenerator.Argument.bodyObject(parameter);
        }

        annotation = getAnnotation(parameter, "org.springframework.web.bind.annotation.RequestHeader");
        if (annotation != null){
            return WebClientGenerator.Argument.header(searchNV(annotation, parameter));
        }

        annotation = getAnnotation(parameter, "org.springframework.web.bind.annotation.RequestParam");
        if (annotation != null){
            return WebClientGenerator.Argument.queryParam(searchNV(annotation, parameter));
        }
        return null;
    }


    @Override
    public String computeRootUri(Class clazz) {
        Object annotation = ReflectUtil.getAnnotation(clazz, "org.springframework.web.bind.annotation.RequestMapping");
        if (annotation != null){
            String[] paths = findPaths(annotation);
            if (paths != null && paths.length > 0){
                return paths[0];
            }
        }
        return "";
    }
}
