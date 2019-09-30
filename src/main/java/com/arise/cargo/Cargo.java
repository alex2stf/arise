package com.arise.cargo;

import static com.arise.core.tools.ListUtil.listContainsIgnorecase;
import static com.arise.core.tools.StringUtil.removeLastChar;

import com.arise.cargo.Plugin.Worker;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.tools.ListUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StreamUtil.LineIterator;
import com.arise.core.tools.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Cargo {

    private static final Mole log = Mole.getInstance(Cargo.class);





    String confPath;
    String[] currentNamespace = new String[]{};
    ARIClazz currentClass;

    ARIDto currentDto;

    Map<String, ARIClazz> clazzez = new HashMap<>();
    Set<ARINum> enums = new HashSet<>();
    Set<ARIDto> dtos = new HashSet<>();
    Set<Plugin> plugins = new HashSet<>();
    List<String> currentLines = new ArrayList<>();
    List<String> currentComments = new ArrayList<>();

    Plugin.Worker currentWorker = null;

    private Set<Context> contexts = new HashSet<>();
    private String[] rootNamespace;

    public static String createClassId(String name, String[] namespaces){
        if (namespaces == null || namespaces.length == 0){
            return name;
        }
        return StringUtil.join(namespaces, ".") + "." + name;
    }

    static Comp calculateComp(String in, int lineNo){
        Comp x = new Comp();
        if (in.indexOf("<") > -1){
            String[] p = in.split("<");
            x.name = p[0].trim();
            String rest = p[1];
            if (rest.endsWith(">")){
                rest  = removeLastChar(rest);
            }
            x.diamonds = rest.split(",");

        } else {
            x.name = in;
        }
        return x;
    }

    public Cargo addPlugin(Plugin plugin){
      plugins.add(plugin);
      return this;
    }

  public Plugin getPlugin(String id) {
    for (Plugin p: plugins){
      if (p.id().equalsIgnoreCase(id)){
        return p;
      }
    }
    return null;
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
        StreamUtil.readLineByLine(inputStream, new LineIterator() {
            @Override
            public void onLine(int lineNo, String content) {
                content = content.trim();

                if(!content.isEmpty()  && !content.startsWith("#")) {
                    if (content.startsWith("!")){
                        currentLines.add(content.substring(1));
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
            System.out.println("    class " + currentClass);
        }

        else if (listContainsIgnorecase("using", queries) &&
            listContainsIgnorecase("type", queries) &&
            listContainsIgnorecase("java", queries) ){
            addType(queries, lineNo);
        }

        else if ("plugin".equalsIgnoreCase(queries[0])){
          System.out.println("required plugin " + queries[1]);
          Plugin plugin = getPlugin(queries[1]);
          if (plugin == null){
              throw new LogicalException("Missing plugin " + queries[1], confPath, lineNo);
          }

          if (queries.length < 3){
              throw new LogicalException("plugin <" + plugin.id() + "> requires at least one worker name ", confPath, lineNo);
          }

          currentWorker = plugin.getWorker(queries[2]);
          if (currentWorker == null){
              throw new LogicalException("Invalid worker " + queries[2] + " for " + plugin);
          }
          currentWorker.setNamespace(currentNamespace);
        }

        else if("->".equalsIgnoreCase(queries[0])){
          String [] args = new String[queries.length - 1];
          for (int i = 1; i < queries.length; i++){
            args[i-1] = queries[i];
          }
          currentWorker.addInstructions(args);
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
        currentDto.addProp(name, thatName, thatType);
    }

    private void addDtoFull(String[] queries, int lineNo) {
        String name = stringAfter("all_from", queries, lineNo, true, confPath);
        String[] excepts = stringsAfter("except", queries);
        currentDto.addFull(name, excepts);
    }

    private void setCurrentDto(String[] queries, int lineNo) {
        String name = stringAfter("dto", queries, lineNo, true, confPath);


        currentDto = new ARIDto(name, currentNamespace);
        currentDto.setLines(currentLines);
        currentDto.setComments(currentComments);
        addAttributes(currentDto, queries, lineNo);
        dtos.add(currentDto);
        currentComments.clear();
        currentLines.clear();

    }

    private void addAttributes(ARILined lined, String[] queries, int lineNo){
        String[] attributes = stringsAfter("attributes:", queries);
        lined.setAttributes(attributes);
    }

    private void pushEnum(String[] queries, int lineNo) {
        String name = queries[1];
        String [] variants = new String[queries.length - 2];
        for (int i = 2; i < variants.length + 2; i++){
            variants[i - 2] = queries[i];
        }

        String[] attributes = stringsAfter("attributes:", queries);

        ARINum ARINum = new ARINum(name, currentNamespace, variants);
        ARINum.setLines(currentLines);
        ARINum.setComments(currentComments);
        ARINum.setAttributes(attributes);
        enums.add(ARINum);

        currentLines.clear();
        currentComments.clear();

        registerType(ARINum);

    }

    private void addProperty(String[] queries, int lineNo) {
        String mxS = stringAfter("maxlength", queries);
        String mnS = stringAfter("minlength", queries);
        String name = stringAfter("var", queries, lineNo, true, confPath);

        if (currentClass.hasPropertyName(name)){
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

        ARIProp prop = new ARIProp.Builder(currentClass, name)
            .setTypeName(typeName)
            .setIsArray(isArray)
            .setAlias(alias)
            .setNoGet(noGet)
            .setNoSet(noSet)
            .setAccessType(accessType).setIsFinal(isFinal)
            .setIsStatic(isStatic)
            .setIsTranzient(isTranzient)
            .setIsPrimaryKey(pKey)
            .setUnique(unique)
            .setFetchType(fetchType)
            .setMaxLength(maxLength)
            .setMinLength(minLength)
            .setDefaultValue(defaultVal)
            .setNullable(nullable)
            .setVolatile(listContainsIgnorecase("volatile", queries))
            .build();

        prop.setComments(currentComments);
        prop.setLines(currentLines);
        addAttributes(prop, queries, lineNo);



        currentClass.addProperty(prop);

        currentLines.clear();
        currentComments.clear();
    }

    private void addType(String[] queries, int lineNo) {
        String compilerName = queries[1].trim();
        String typeName = stringAfter("type", queries, lineNo, true, confPath);
        String requirement = stringAfter("require", queries);



        Context context = getCompilerById(compilerName);
        boolean iterable = listContainsIgnorecase("iterable", queries);
        String fullName[] = stringsAfter(typeName, queries);

        if (fullName == null || fullName.length == 0){
            throw new SyntaxException("Cannot register type " + typeName + " without a fullName");
        }

        if (context != null){
            context.registerType(typeName, StringUtil.join(fullName, " "), iterable, requirement);
        } else {
            for (Context c: contexts){
                c.registerType(typeName, StringUtil.join(fullName, " "), iterable, requirement);
            }
        }
    }

    Context getCompilerById(String id){
        for (Context c: contexts){
            if (id.equalsIgnoreCase(c.getId())){
                return c;
            }
        }
        return null;
    }

    private void setCurrentClass(String[] queries, int lineNo) {
        ClazzMode clazzMode = ClazzMode.STANDARD;
        boolean persistable = listContainsIgnorecase("persistable", queries);
        boolean embeddable = listContainsIgnorecase("embeddable", queries);
        boolean isFinal = listContainsIgnorecase("final", queries);
        boolean isAbstract = listContainsIgnorecase("abstract", queries);
        boolean iterable = listContainsIgnorecase("iterable", queries);
        String fullname = stringAfter("class", queries);

        if (fullname == null){
            fullname = stringAfter("interface", queries, lineNo, true, confPath);
            clazzMode = ClazzMode.INTERFACE;
            isAbstract = true;
        }

        String tableName = stringAfter("alias", queries);

        if (tableName == null){
            tableName = stringAfter("table", queries);
        }

        String schema =  stringAfter("schema", queries);

        Comp c = calculateComp(fullname, lineNo);


        if (listContainsIgnorecase("abstract", queries)){
            clazzMode = ClazzMode.ABSTRACT;
        }

        AccessType accessType = getAccessType(queries, lineNo);
        String[] extendName = stringBetween("extends", queries, "implements");
        String[] implementsNames = stringsAfter("implements", queries);

        currentClass = new ARIClazz(c.name, currentNamespace, persistable, embeddable, clazzMode, isFinal, isAbstract, accessType, extendName,
            implementsNames, tableName, schema, iterable, c.diamonds);
        addAttributes(currentClass, queries, lineNo);
        currentClass.setLines(currentLines);
        currentClass.setComments(currentComments);

        clazzez.put(currentClass.getId(), currentClass);

        registerType(currentClass);
        currentLines.clear();
        currentComments.clear();
     }

     private void registerType(ARIType t){
        for (Context c: contexts){
            c.registerType(t);
        }
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
         return ListUtil.toArray(s);

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
        return ListUtil.toArray(r);
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

    public Cargo compile() {

        for (Context context : contexts){
            context
                .setRootNamespace(rootNamespace)
                .setClasses(clazzez)
                .setDtos(dtos)
                .setEnums(enums)
                .setPlugins(plugins)
                .validateDataTypes()
                .compile();
        }


        return this;
    }

    public Cargo addContext(Context context) {
        this.contexts.add(context);
        return this;
    }

    public Cargo loadBasicTypes() {
        this.read(StreamUtil.readResource("_cargo_/basic_types.cargo"));
        return this;
    }

    static class Comp {
        public String name;
        public String[] diamonds;
    }
}
