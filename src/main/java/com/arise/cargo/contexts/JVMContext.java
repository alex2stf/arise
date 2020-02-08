package com.arise.cargo.contexts;

import com.arise.cargo.model.AccessType;
import com.arise.cargo.model.CGClass;
import com.arise.cargo.model.CGMethod;
import com.arise.cargo.model.CGType;
import com.arise.cargo.model.CGVar;
import com.arise.cargo.model.ClassFlavour;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.models.FilterCriteria;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.*;

import static com.arise.core.tools.StringUtil.capFirst;

public class JVMContext extends StrongTypedContext {
    public JVMContext() {
        super("java");
    }

    @Override
    public void compile() {
        for (CGClass cgClass: classes){
            cgClass.setParentContext(this);
            InputStream inputStream = StreamUtil.readResource("/templates/jvclz.whiskey");
            StringWriter stringWriter = new StringWriter();
            try {
                new Whisker().compile(new InputStreamReader(inputStream), stringWriter, cgClass);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(stringWriter.toString());

            File outDir = new File("src/test/java/", cgClass.joinNamespace(File.separator));
            if (!outDir.exists()){
                outDir.mkdirs();
            }

            File out = new File(outDir.getAbsolutePath(), cgClass.getName() + ".java");
            FileUtil.writeStringToFile(out, stringWriter.toString());
//            System.out.println(outDir.getAbsolutePath());
//            System.out.println(outDir.exists());

        }
    }


    private CGType[] getImportsForClass(CGClass cgClass){
        Set<CGType> imports = new HashSet<>();
        scan(getTypesByName(cgClass.getExtend(), cgClass), imports, cgClass);
        scan(getTypesByName(cgClass.getImplement(), cgClass), imports, cgClass);
        for (CGVar var: cgClass.getProperties()){
            scan(getTypesByName(new String[]{var.getType()}, cgClass), imports, cgClass);

            if (var.hasTypedParameters()){
                for (String s: var.getTypedParameters()){
                    CGType type = getTypeByName(s, cgClass);
                    if (allow(type, cgClass)){
                        imports.add(type);
                    }
                }
            }
        }


        CGType[] r = new CGType[imports.size()];
        imports.toArray(r);
        return r;
    }

    public void scan(List<CGType> types, Set<CGType> buffer, CGClass classContext){
        for (CGType t: types){

            if (allow(t, classContext)){
                buffer.add(t);
            }
        }
    }

    public boolean allow(CGType t, CGClass classContext){
        boolean allow1 = t.getNativeIdentifier() == null ? true : !t.getNativeIdentifier().startsWith("java.lang");
        boolean isSeparatePackage = !t.getPackage().equalsIgnoreCase(classContext.getPackage());
        boolean allow2 = !"char".equals(t.getNativeIdentifier()) && !"long".equals(t.getNativeIdentifier())
                && !"boolean".equals(t.getNativeIdentifier());
        return allow1 && allow2 && isSeparatePackage && !t.isAbstractParameterType();
    }

    @Override
    public CGType[] calculateImports(Object any) {
        if (any instanceof CGClass){
            return getImportsForClass((CGClass) any);

        }
        return null;
    }

    @Override
    public String calculateVariableType(CGVar cgVar) {
        CGType cgType;
        try {
            cgType = getTypeByName(cgVar.getType(), null);
        }catch (SyntaxException ex){
            //it might be a parametrized type
            return cgVar.getType();
        }

        if (cgType.getNativeName() != null){

            if (cgType.hasTypeParameters()){
                List<CGType> parametersType = getTypesByName(cgVar.getTypedParameters(), cgVar);
                return cgType.getNativeName() + "<" + StringUtil.join(parametersType, ", ", new StringUtil.JoinIterator<CGType>() {
                    @Override
                    public String toString(CGType value) {
                        return value.getNativeName() != null ? value.getNativeName() : value.getName();
                    }
                }) + ">";
            }

            return cgType.getNativeName();
        }

        return cgType.getName();
    }

    @Override
    public String solveKeyword(CGClass cgClass) {
        if (cgClass.getFlavour().equals(ClassFlavour.INTERFACE)){
            return "interface";
        }

        if (cgClass.getFlavour().equals(ClassFlavour.ENUM)){
            return "enum";
        }
        return "class";
    }

    @Override
    public List<CGMethod> calculateSetters(CGClass cgClass) {
        List<CGMethod> methods = new ArrayList<>();
        for (CGVar cgVar: cgClass.getProperties()){
            if (cgVar.getAccessType().equals(AccessType.PRIVATE) || cgVar.getAccessType().equals(AccessType.DEFAULT) || cgVar.getAccessType().equals(AccessType.PROTECTED) ) {
                if (cgVar.isAllowGetter()){
                    String name = "set" + capFirst(cgVar.getName());
                    String varType = calculateVariableType(cgVar);
                    String returnType = cgVar.getContainerClass().getName();


                    CGMethod method = new CGMethod().setName(name)
                            .setReturnType(returnType)
                            .setArguments(new CGMethod.Args(varType, cgVar.getName()))
                            .addBodyLine("this." + cgVar.getName() + " = " + cgVar.getName())
                            .addBodyLine("return this")
                            .setAccessType(AccessType.PUBLIC)
                            .setParentContext(this);
                    methods.add(method);
                }
            }
        }
        return methods;
    }


    @Override
    public List<CGMethod> calculateGetters(CGClass cgClass) {
        List<CGMethod> methods = new ArrayList<>();
        for (CGVar cgVar: cgClass.getProperties()){
            if (cgVar.getAccessType().equals(AccessType.PRIVATE) || cgVar.getAccessType().equals(AccessType.DEFAULT) || cgVar.getAccessType().equals(AccessType.PROTECTED)) {
               if (cgVar.isAllowGetter()){
                    String name = "get" + capFirst(cgVar.getName());
                    String varType = calculateVariableType(cgVar);

                    CGMethod method = new CGMethod().setName(name)
                            .setReturnType(varType)
                            .addBodyLine("return " + cgVar.getName())
                            .setAccessType(AccessType.PUBLIC)
                            .setParentContext(this);
                    methods.add(method);
               }
            }
        }
        return methods;
    }

    @Override
    public String calculateArgumentsLine(CGMethod cgMethod) {
        return StringUtil.join(cgMethod.getArgs(), ", ", new StringUtil.JoinIterator<CGMethod.Args>() {
            @Override
            public String toString(CGMethod.Args value) {
                return value.getType() + " " + value.getName();
            }
        });
    }

    @Override
    public List<CGVar> solveNonStaticVars(CGClass cgClass) {
        List<CGVar> res = filter(new FilterCriteria<CGVar>() {
            @Override
            public boolean isAcceptable(CGVar data) {
                return data.getAccessType().equals(AccessType.PUBLIC) && !data.isStatic();
            }
        }, cgClass);

        List<CGVar> protect = filter(new FilterCriteria<CGVar>() {
            @Override
            public boolean isAcceptable(CGVar data) {
                return data.getAccessType().equals(AccessType.PROTECTED) && !data.isStatic();
            }
        }, cgClass);

        for (CGVar var: protect){
            res.add(var);
        }

        List<CGVar> dflt = filter(new FilterCriteria<CGVar>() {
            @Override
            public boolean isAcceptable(CGVar data) {
                return data.getAccessType().equals(AccessType.DEFAULT) && !data.isStatic();
            }
        }, cgClass);

        for (CGVar var: dflt){
            res.add(var);
        }

        List<CGVar> priv = filter(new FilterCriteria<CGVar>() {
            @Override
            public boolean isAcceptable(CGVar data) {
                return data.getAccessType().equals(AccessType.PRIVATE) && !data.isStatic();
            }
        }, cgClass);

        for (CGVar var: priv){
            res.add(var);
        }

        return res;
    }



    Comparator<CGVar> BY_NAME = new Comparator<CGVar>() {
        @Override
        public int compare(CGVar o1, CGVar o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    List<CGVar> filter(FilterCriteria<CGVar> filterCriteria, CGClass cgClass){
        List<CGVar> res = new ArrayList<>();
        for (CGVar cgVar: cgClass.getProperties()){
            if (filterCriteria.isAcceptable(cgVar)){
                res.add(cgVar);
            }
        }
        Collections.sort(res, BY_NAME);
        return res;
    }
}
