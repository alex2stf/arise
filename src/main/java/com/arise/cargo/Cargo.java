package com.arise.cargo;


import com.arise.cargo.model.*;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StreamUtil.LineIterator;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.arise.core.tools.CollectionUtil.listContainsIgnorecase;
import static com.arise.core.tools.StreamUtil.readLineByLine;
import static com.arise.core.tools.StringUtil.removeLastChar;

public class Cargo {

    private static final Mole log = Mole.getInstance(Cargo.class);





    String confPath;
    String[] currentNamespace = new String[]{};

    List<String> currentLines = new ArrayList<>();
    List<String> currentComments = new ArrayList<>();

    private String[] rootNamespace;


    private Set<Context> contextSet = new HashSet<>();

    public Cargo addContext(Context context){
        contextSet.add(context);
        return this;
    }






    public Cargo readFromFile(String file) {
        return read(new File(file));
    }

    public Cargo read(File f){
        confPath = f.getAbsolutePath();
        try {
            return read(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Cargo read(InputStream inputStream) {
        readLineByLine(inputStream, new LineIterator() {
            @Override
            public void onLine(int lineNo, String content) {
                content = content.trim();

                if(!content.isEmpty()  && !content.startsWith("#")) {
                    if (content.startsWith("!")){
                        currentLines.add(content.substring(1).trim());
                    }
                    else if (content.startsWith("?")){
                        currentComments.add(content.substring(1).trim());
                    }
                    else {
                        String[] queries = content.split("\\s+");
                        digest(lineNo, queries);
                    }
                }
            }
        });
        return this;
    }

    private void digest(int lineNo, String[] queries) {

        if ("namespace".equalsIgnoreCase(queries[0])){
            System.out.println("    namespace " + queries[1]);
            currentNamespace = queries[1].split("\\.");
        }

        else if ("root-namespace".equalsIgnoreCase(queries[0])){
            rootNamespace = queries[1].split("\\.");
        }

        else if ("enum".equalsIgnoreCase(queries[0])){
            pushEnum(queries, lineNo);
        }

        else if (listContainsIgnorecase("class", queries) || listContainsIgnorecase("interface", queries) ) {
            if (listContainsIgnorecase("var", queries)){
                throw new SyntaxException("'var' and 'class/interface' are keywords that cannot be part of the same line", confPath, lineNo);
            }
            setCurrentClass(queries, lineNo);
            currentLines = new ArrayList<>(); //clear existing currentLines after creating a class
            currentComments = new ArrayList<>(); //clear existing comments after creating a class

        }

        else if (listContainsIgnorecase("using", queries) ){
            addType(queries, lineNo);
        }

        else if ("plugin".equalsIgnoreCase(queries[0])){
          System.out.println("required plugin " + queries[1]);
        }

        else if("->".equalsIgnoreCase(queries[0])){
          String [] args = new String[queries.length - 1];
          for (int i = 1; i < queries.length; i++){
            args[i-1] = queries[i];
          }
        }

        else if ("dto".equalsIgnoreCase(queries[0])){
            setCurrentDto(queries, lineNo);
        }

        else if ("all_from".equalsIgnoreCase(queries[0])){
            addDtoFull(queries, lineNo);
        }

        else if ("prop".equalsIgnoreCase(queries[0])){
            addDtoProp(queries, lineNo);
        }

        else if (listContainsIgnorecase("var", queries)){
            if (listContainsIgnorecase("class", queries) || listContainsIgnorecase("interface", queries)){
                throw new SyntaxException("'class/interface' and 'var' are keywords that cannot be part of the same line", confPath, lineNo);
            }

            addProperty(queries, lineNo);
            currentLines = new ArrayList<>(); //clear existing currentLines after creating a class
            currentComments = new ArrayList<>(); //clear existing comments after creating a class
        }

        else if(listContainsIgnorecase("embeddable", queries) && !listContainsIgnorecase("class", queries)){
            throw new LogicalException("[embeddable] keyword needs to be linked with a class");
        }


        else {
            System.out.println(lineNo + " " + StringUtil.join(queries, " - "));
        }

    }

    private void addDtoProp(String[] queries, int lineNo) {
        String name = stringAfter("prop", queries, lineNo, true, confPath);
        String thatType = stringAfter("from", queries, lineNo, true, confPath);
        String thatName = stringAfter(thatType, queries, lineNo, true, confPath);

    }

    private void addDtoFull(String[] queries, int lineNo) {
        String name = stringAfter("all_from", queries, lineNo, true, confPath);
        String[] excepts = stringsAfter("except", queries);

    }

    private void setCurrentDto(String[] queries, int lineNo) {
        String name = stringAfter("dto", queries, lineNo, true, confPath);





    }



    private void pushEnum(String[] queries, int lineNo) {
        System.out.println("ENUM" + StringUtil.join(queries, ""));

    }

    private void addProperty(String[] queries, int lineNo){
        for (Context context: contextSet){
            addProperty(context.getCurrentClass(), queries, lineNo);
        }
    }

    private void addProperty(CGClass currentClass, String[] queries, int lineNo) {
        String mxS = stringAfter("maxlength", queries);
        String mnS = stringAfter("minlength", queries);
        String name = stringAfter("var", queries, lineNo, true, confPath);

        if (!StringUtil.hasText(name)){
            throw new SyntaxException("Property without name is not allowed" + currentClass.getName() + "]", confPath, lineNo);
        }

        if (currentClass.hasProperty(name)){
            throw new SyntaxException("Property [" + name + "] already exists for class [" + currentClass.getName() + "]", confPath, lineNo);
        }

        String typeName = stringAfter(name, queries, lineNo, true, confPath);
        boolean isArray = false;
        if (typeName.endsWith("[]")){
            isArray = true;
            typeName = removeLastChar(removeLastChar(typeName));
        }






        String alias = stringAfter("alias", queries);
        boolean noGet = listContainsIgnorecase("noget", queries);
        boolean noSet =  listContainsIgnorecase("noset", queries);
        AccessType accessType = getAccessType(queries, lineNo);
        boolean isFinal = listContainsIgnorecase("final", queries);
        boolean isStatic = listContainsIgnorecase("static", queries);
        boolean isTranzient  = listContainsIgnorecase("transient", queries);
        boolean pKey = listContainsIgnorecase("primaryKey", queries);
        boolean unique = listContainsIgnorecase("unique", queries);
        String fetchType = stringAfter("fetch", queries);

        Integer maxLength  = StringUtil.toInt(mxS);
        Integer minLength = StringUtil.toInt(mnS);
        String defaultVal = stringAfter("default", queries);
        boolean nullable = true;
        if (listContainsIgnorecase("notnull", queries)){
            nullable = false;
        }
        ParseUtil.Composition composition = ParseUtil.parse(typeName);
        CGVar cgVar = new CGVar()
                .setArray(isArray)
                .setAlias(alias)
                .setAllowGetter(!noGet)
                .setAllowSetter(!noSet)
                .setAccessType(accessType)
                .setFinal(isFinal)
                .setStatic(isStatic)
                .setTranzient(isTranzient)
                .setPrimaryKey(pKey)
                .setUnique(unique)
                .setFetchType(fetchType)
                .setMaxlength(maxLength)
                .setMinlength(minLength)
                .setDefaultValue(defaultVal)
                .setName(name)
                .setType(composition.getName())
                .setTypedParameters(composition.getTypedParameters())
                .setNullable(nullable)
                .setCommentLines(currentLines)
                .setCommentBlocks(currentComments);

        currentClass.addProperty(cgVar);

        currentLines.clear();
        currentComments.clear();
    }

    private void addType(String[] queries, int lineNo) {
        String compilerName = queries[1].trim();
        boolean iterable = "iterable".equalsIgnoreCase(queries[2]);
        String typeName = stringAfter("type", queries, lineNo, true, confPath); //Map<?,?>
        String requirement = stringAfter("require", queries);

        for (Context c: contextSet){
            if (c.getId().equals(compilerName)){
                ParseUtil.Composition composition = ParseUtil.parse(queries[queries.length -1]);

                CGType cgType = new CGType().setName(typeName)
                        .setRequirement(requirement)
                        .setIterable(iterable)
                        .setNativeIdentifier(composition.getName())
                        .setTypedParameters(composition.getTypedParameters());
                c.registerType(cgType);
            }
        }


    }



    private void setCurrentClass(String[] queries, int lineNo) {
        ClassFlavour classFlavour = ClassFlavour.CLAZZ;
        boolean persistable = listContainsIgnorecase("persistable", queries);
        boolean embeddable = listContainsIgnorecase("embeddable", queries);
        boolean isFinal = listContainsIgnorecase("final", queries);
        boolean isAbstract = listContainsIgnorecase("abstract", queries);
        boolean iterable = listContainsIgnorecase("iterable", queries);
        String fullname = stringAfter("class", queries);

        if (fullname == null){
            fullname = stringAfter("interface", queries, lineNo, true, confPath);
            classFlavour = ClassFlavour.INTERFACE;
            isAbstract = true;
        }

        String tableName = stringAfter("alias", queries);

        if (tableName == null){
            tableName = stringAfter("table", queries);
        }

        String schema =  stringAfter("schema", queries);


        if (listContainsIgnorecase("abstract", queries)){
            classFlavour = ClassFlavour.ABSTRACT;
            isAbstract = true;
        }

        AccessType accessType = getAccessType(queries, lineNo);
        String[] extendName = stringBetween("extends", queries, "implements");
        String[] implementsNames = stringsAfter("implements", queries);

        ParseUtil.Composition composition = ParseUtil.parse(fullname);





        for (Context context: contextSet){

            CGClass currentClass = new CGClass()
                    .setAbstract(isAbstract)
                    .setAccessType(accessType)
                    .setEmbeddable(embeddable)
                    .setExtend(extendName)
                    .setImplement(implementsNames)
                    .setPersistable(persistable)
                    .setIterable(iterable)
                    .setName(composition.getName())
                    .setTable(tableName)
                    .setSchema(schema)
                    .setFinal(isFinal)
                    .setFlavour(classFlavour)
                    .setNamespace(currentNamespace)
                    .setCommentLines(currentComments)
                    .setCommentBlocks(currentLines)
                    .setTypedParameters(composition.getTypedParameters());

            context.setCurrentClass(currentClass);

        }


        currentLines.clear();
        currentComments.clear();
     }


     private String[] stringBetween( String start, String[] args, String end){
         boolean allow = false;
         List<String> s = new ArrayList<>();
         for(int i = 0; i < args.length; i++){
             if (end.equalsIgnoreCase(args[i])){
                 break;
             }

             if (allow){
                 s.add(args[i]);
             }

             if (start.equalsIgnoreCase(args[i])){
                allow = true;
             }

         }
         return CollectionUtil.toArray(s);

     }

    public static String[] stringsAfter(String search, String[] queries) {
        List<String> r = new ArrayList<>();
        boolean accept = false;
        for (int i = 0; i < queries.length; i++){
            if (accept){
                r.add(queries[i]);
            }

            if (search.equalsIgnoreCase(queries[i])) {
                accept = true;
            }
        }
        return CollectionUtil.toArray(r);
    }

    private AccessType getAccessType(String[] queries, int lineNo) {
        if (listContainsIgnorecase("private", queries)){
            return AccessType.PRIVATE;
        }
        else if (listContainsIgnorecase("public", queries)){
            return AccessType.PUBLIC;
        }
        else if (listContainsIgnorecase("protected", queries)){
            return AccessType.PROTECTED;
        }
        return AccessType.DEFAULT;
    }

    public static String stringAfter(String s, String[] items){
        return stringAfter(s, items, 0, false, null);
    }

    public static String stringAfter(String s, String[] items, int lineNo, boolean throwErr, String confPath){
        for (int i = 0; i < items.length; i++){
            if (s.equalsIgnoreCase(items[i])){
                return items[i+1];
            }
        }
        if (throwErr){
            throw new SyntaxException("Could not find [" + s + "]", confPath, lineNo);
        }
        return null;
    }




    public Cargo loadBasicTypes() {
        this.read(StreamUtil.readResource("_cargo_/basic_types.cargo"));
        return this;
    }


}
