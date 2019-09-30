package com.arise.cargo;


import com.arise.cargo.Cargo.Comp;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.FilterCriteria;
import com.arise.core.tools.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Context {

    public static final String[] alpha = "ABCDEFGHIJKLMNOPQRSTUVXYZ".split("");

    private final String id;
    private final Set<Profile> profiles = new HashSet<Profile>();
    protected Map<String, ARIType> langTypes = new HashMap<>();
    private File output;
    private Map<String, ARIClazz> classes;
    private String projectName;



    private HashMap<Option, String> options = new HashMap<>();
    private Set<ARINum> enums;
    private Set<ARIDto> dtos;
    private String libName;
    private String[] rootNamespace;
  private Set<Plugin> plugins;


  protected Context(String id){
        this.id = id;
    }

    public String[] getRootNamespace() {
      return rootNamespace;
    }

    public Context setRootNamespace(String[] rootNamespace) {
      this.rootNamespace = rootNamespace;
      return this;
    }

    public Context setOption(Option option, String value){
      options.put(option, value);
      return this;
    }

    public Context setOption(Option option, boolean value){
      options.put(option, String.valueOf(value));
      return this;
    }

    public boolean isEnabled(Option option){
      return options.containsKey(option) && "true".equalsIgnoreCase(options.get(option));
  }

    public String getOption(Option option){
      return options.get(option);
    }

    public Set<ARINum> getEnums() {
        return enums;
    }

    public final Context setEnums(Set<ARINum> enums){
        this.enums = enums;
        return this;
    }

    public Set<ARIDto> getDtos() {
        return dtos;
    }

    public final Context setDtos(Set<ARIDto> dtos) {
        this.dtos = dtos;
        return this;
    }

    public String getLibName() {
        return libName;
    }

    public  Context setLibName(String libName){
        this.libName = libName;
        return this;
    }

    public Map<String, ARIType> getTypes() {
        return langTypes;
    }

    public File getOutput(){
        return output;
    }

    public final Context setOutput(String outDir) {
        output = new File(outDir);
        return this;
    }

    Context setOutput(File output){
        this.output = output;
        return this;
    }

    public String getId() {
        return id;
    }

    public abstract void compile();


    public Map<String, ARIClazz> getClasses() {
        return classes;
    }

    public final Context setClasses(Map<String, ARIClazz> clazzez) {
        this.classes = clazzez;
        return this;
    }

    public final void registerType(ARIType t){
       if (langTypes.containsKey(t.getName())){
           ARIType that = langTypes.get(t.getName());
           if (that.packId().equals(t.packId()) && t instanceof ARIClazz){
               System.out.println(t.getId() + " exists by name");
               langTypes.put(t.getName(), t);
               localTypeRegistered(t);
               return;
           }
       }
       langTypes.put(t.getId(), t);
       localTypeRegistered(t);
    }

   final void registerType(String id, String fullName, boolean iterable, String requirement) {
        id = id.trim();


        ARIType t;
        if (fullName.indexOf(".") > -1) {
            String[] parts = fullName.split("\\.");
            String[] nmsps = new String[parts.length - 1];
            for (int i = 0; i < nmsps.length; i++) {
                nmsps[i] = parts[i];
            }
            t = new ARIType(parts[parts.length - 1], nmsps, iterable);
        } else {
            t = new ARIType(fullName, null, iterable);
        }
        t.setRequirement(requirement);
        langTypes.put(id, t);
        globalTypeRegistered(t);
    }

    protected void localTypeRegistered(ARIType type){

    }

    protected void globalTypeRegistered(ARIType type){

    }


    public ARIType getTypeByName(String in) {
        Comp comp = Cargo.calculateComp(in, 0);
        String name = comp.name;

        ARIType response = null;
        if (langTypes.containsKey(name)){
            response = langTypes.get(name);
        }
        if (response == null) {
            for (ARIType x : langTypes.values()) {
                if (name.equals(x.getName())) {
                    response = x;
                    break;
                }
            }
        }

        if (in.startsWith("?") && in.length() > 1){
            String dIndex = in.substring(1);
            try {
                int n = Integer.valueOf(dIndex);
                response = new ARIGeneric(Context.alpha[n], n);
            }catch (Exception ex){
                throw new SyntaxException("cannot determine type for " + in, ex);
            }
        }

        if (response == null){
            throw new SyntaxException("Type " + in + " not found in context " + this.getId());
        }


        if (response.hasDiamonds() && comp.diamonds != null && comp.diamonds.length > 0) {
            if (response.getDiamondNames().length != comp.diamonds.length){
                throw new SyntaxException("Insufficient implementations for type " + in + " (added an extra char between <?,?>)");
            }
            List<ARIType> diamondImplementations = new ArrayList<>();

            int calph = 0;
            for (int i = 0; i < comp.diamonds.length; i++){
                String s = comp.diamonds[i];
                if (!"?".equals(s)){
                    diamondImplementations.add(getTypeByName(s));
                } else {
                    diamondImplementations.add(new ARIGeneric(alpha[calph], i));
                }
                calph++;
            }
            response.setDiamondImplementations(diamondImplementations);
        } else {
            response.setDiamondImplementations(new ArrayList<ARIType>());
        }
        return response;
    }

    public String buildGetterName(String input){
        return "get" + StringUtil.capFirst(input);
    }

    public String buildSetterName(String input){
        return "set" + StringUtil.capFirst(input);
    }

    protected Set<String> getUniqueRequirements(){
        Set<String> resp = new HashSet<>();
        for (ARIType crgClass: langTypes.values()){
            String requirement = crgClass.getRequirement();

            if (requirement != null && !resp.contains(requirement)){
                resp.add(requirement);
            }
        }
        return resp;
    }

    public Context withProfiles(String ... args) {
        for (String s: args){
            if ((!profiles.contains(s))){
                Profile p = Profile.DefaultFactory.get(s, this);
                if (p != null){
                  profiles.add(p);
                }

            }
        }
        return this;
    }

    public final List<String> filterLines(ARILined lined, FilterCriteria<String> filterCriteria){
        List<String> lines = new ArrayList<>();
        if (lines == null || lines.isEmpty()){
            return Collections.emptyList();
        }

        for (String s: lined.getLines()){
            System.out.println("----------------------------|" + s);
            String[] p = StringUtil.splitByWhitespace(s);
            String id = p[0];
            String line = null;
            if (id.startsWith(this.id)) {


                if (id.indexOf(":") > -1){
                    String[] p2 = id.split(":");
                    if (p2.length == 2){
                        String contextId = p2[0].trim();
                        String profile = p2[1];
                        if (this.id.equals(contextId)){
                            if (this.hasProfile(profile)){
                                line = StringUtil.join(p, 1, p.length, " ", StringUtil.DEFAULT_ITERATOR);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("line id invalid at [" + s + "]");
                    }
                } else {
                    //no profile defined
                    line = s.substring(this.id.length(), line.length() -1);
                }
                System.out.println("--------------------------xx|" + line);

            } else {
                System.out.println("context " + this.id + " ignoring line " + s);
            }

            if (line != null && filterCriteria.isAcceptable(line)){
                lines.add(line);
            }
        }
        return lines;
    }

    public final boolean hasProfile(String profile) {
        return profiles.contains(profile);
    }

    public final boolean hasAnyOfTheProfiles(String ... args) {
       for (Profile s: profiles){
           for (String a: args){
               if (s.id().equals(a)){
                   return true;
               }
           }
       }
       return false;
    }

    public Context validateDataTypes(){
        for (PrimitiveType t: PrimitiveType.values()){
            if(!langTypes.keySet().contains(t.name().toLowerCase())){
                throw new SyntaxException("[" + this.id + "] no type mapping found for primitive [" + t.name().toLowerCase() + "]");
            }

        }

        return this;
    }

    public final String getProjectName(){
        return projectName;
    }

    public final Context setProjectName(String projectName){
        this.projectName = projectName;
        return this;
    }



  public boolean isPrimitive(ARIProp prop){
    for (PrimitiveType t: PrimitiveType.values()){
      if (prop.getTypeName().toLowerCase().equalsIgnoreCase(t.name().toLowerCase())){
        return true;
      }
    }
    return false;
  }

  protected final void onBeforeWritingMethod(BlockBuilder builder, Set<String> imports, boolean persistable, ARIMethod method){
      for (Profile p: profiles){
        p.onBeforeWritingMethod(builder, imports, persistable, method);
      }
  }

  protected final void onBeforeWritingClass(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports){
    for (Profile p : profiles){
      p.onBeforeWritingClass(blockBuilder, clazz, imports);
//      profileWriter.writeClassAnnotations(blockBuilder, clazz, imports);
    }
  }

  protected final void onBeforeWritingProperties(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports){
      for (Profile p : profiles){
        p.onBeforeWritingProperties(blockBuilder, clazz, imports);
      }
  }

  protected final void onBeforeWritingProperty(BlockBuilder blockBuilder, ARIProp prop, ARIType context, Set<String> imports, String mode){
    for (Profile p : profiles){
      p.onBeforeWritingProperty(blockBuilder, prop, context, imports, mode);
    }
  }

  protected final void onBeforeWritingGetSet(BlockBuilder blockBuilder, ARIProp source, ARIMethod method, Set<String> imports){
      for (Profile p: profiles){
        p.onBeforeWritingGetSet(blockBuilder, source, method, imports);
      }
  }

  protected Set<Profile> getProfiles() {
    return profiles;
  }

  public void addProfile(Profile profile) {
    this.profiles.add(profile);
    if (profile.getContext() != this){
      throw new LogicalException("Cannot have different context");
    }
  }

  public Context setPlugins(Set<Plugin> plugins) {
    this.plugins = plugins;
    return this;
  }

  public Set<Plugin> getPlugins(){
      return plugins;
  }

  public enum  Option {
      SETTER_CHAINED,
      SETTER_VOID,
      SETTER_MIXED
  }
}