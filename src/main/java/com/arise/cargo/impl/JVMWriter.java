package com.arise.cargo.impl;

import static com.arise.cargo.Context.Option.SETTER_CHAINED;
import static com.arise.cargo.Context.Option.SETTER_MIXED;
import static com.arise.cargo.Context.Option.SETTER_VOID;
import static com.arise.core.tools.FileUtil.writeStringToFile;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIDto;
import com.arise.cargo.ARINum;
import com.arise.cargo.ARILined;
import com.arise.cargo.ARIMethod;
import com.arise.cargo.ARIMethod.Arg;
import com.arise.cargo.ARIMethod.Getter;
import com.arise.cargo.ARIMethod.Setter;
import com.arise.cargo.ARIMethod.Sourced;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.cargo.Plugin;
import com.arise.cargo.Plugin.Worker;
import com.arise.cargo.PrimitiveType;
import com.arise.cargo.Profile;
import com.arise.cargo.impl.profiles.ProjectProfile;
import com.arise.core.tools.BlockBuilder;
import com.arise.cargo.AccessType;
import com.arise.cargo.ClazzMode;
import com.arise.core.tools.FilterCriteria;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.StringUtil.JoinIterator;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JVMWriter extends Context {




    public JVMWriter() {
        super("java");
    }

    private void compileAll(File out){
      for (ARIClazz c : getClasses().values()) {
        compileClass(c, out);
      }

      for (ARINum e : getEnums()) {
        writeEnum(e, out);
      }

      for (ARIDto dto : getDtos()) {
        writeDTO(dto, out);
      }

      for (Plugin plugin: this.getPlugins()){
        for (Worker worker: plugin.getWorkers()){
          worker.execute(this, out);
        }
      }
    }

    @Override
    public void compile() {


      boolean writtenByProject = false;
      for (Profile p: this.getProfiles()){
        if (p instanceof ProjectProfile){
          ProjectProfile projectProfile = (ProjectProfile) p;
          File out = projectProfile.redefineOutput(getOutput());
          compileAll(out);
          writtenByProject = true;
          projectProfile.compilationDone(getOutput());
        }
      }

      if (!writtenByProject){
        compileAll(getOutput());
      }


//        String pomPath = null;
//        //write
//        if (this.hasProfile("maven")){
//            pomPath = this.getOutput().getAbsolutePath();
//            File out = FileUtil.extendPack(this.getOutput(), "src", "main", "java");
//            setOutput(out.getAbsolutePath());
//        }
//
//
//
//
//
//
//        //write
//        if (this.hasProfile("maven")){
//            MavenWriter mavenWriter = new MavenWriter("com.readyup", this.getProjectName(), "1.0")
//                .setProjectDependencies(this.getDependencies());
//            if(pomPath == null ){
//                throw new RuntimeException("WTF??");
//            }
//
//           if (!pomPath.endsWith(File.separator)){
//                pomPath += File.separator;
//           }
//           pomPath += "pom.xml";
//
//            FileUtil.writeStringToFile(new File(pomPath), mavenWriter.toString());
//        }
    }

    private void writeDTO(ARIDto arg, File outputDir) {
        BlockBuilder blockBuilder = new BlockBuilder();

        Set<String> imports = new HashSet<>();

        addImports(arg.getRequiredTypes(this), arg, imports);

        blockBuilder.writeLine("public class ", arg.getName(), " {");

        blockBuilder.endl();
        writeProps(blockBuilder, arg.getProperties(this), arg, imports, "dto");
        blockBuilder.endl();

        blockBuilder.getBlock("cmnt2").writeLine("//static mappers");

        for (ARIType t : arg.getSources(this)) {
            if (t instanceof ARIClazz) {
                writeTransferFromMethod(blockBuilder, arg, (ARIClazz) t);
            }
        }

        writeGettersAndSetters(arg.gettersAndSettersList(this), blockBuilder, arg, imports);

        List<Sourced> extraSetters = arg.getExtraSetters(this);
        if (!extraSetters.isEmpty()) {
            blockBuilder.getBlock("cmnt0").writeLine("//custom setters:");

            for (Sourced m : extraSetters) {
                BlockBuilder xtra = blockBuilder.getBlock(m.getName() + "xtra");
                Arg args = m.getArguments(this).get(0); //only 1 argument allowed

                this.onBeforeWritingMethod(xtra, imports, false, m);

                xtra.writeLine(
                    "public ", getQualifiedName(arg),
                    " ", m.getName(), "(", getQualifiedName(args.getType()), " ", arg.getName(), ") {"
                );

                xtra.getBlock("body").writeLine(
                    "this.", m.getName(), "(", arg.getName(), ".", buildGetterName(m.source().getName()), "());"
                )
                    .writeLine("return this;");

                xtra.writeLine("}").endl();
            }
        }

        blockBuilder.endl();

        blockBuilder.getBlock("cmnt").writeLine("//composition getters");

        //write transfer methods:
        for (ARIType t : arg.getSources(this)) {
            if (t instanceof ARIClazz) {
                writeTransferToMethod(blockBuilder, arg, (ARIClazz) t, imports);
            }
        }

        //write builder class
        Set<ARIType> sourceTypes = arg.getBuilders(this);
        if (sourceTypes.size() > 1) {
            BlockBuilder innerClass = blockBuilder.getBlock("inner" + arg.getName());

            innerClass.writeLine("public static final class Builder {");

            String dtovar = StringUtil.lowFirst(arg.getName());

            innerClass.getBlock("vars").writeLine("private ", getQualifiedName(arg),
                " ", dtovar, " =  new ", getQualifiedName(arg), "();");

            innerClass.endl();

            for (ARIType s : sourceTypes) {

                String vname = StringUtil.lowFirst(s.getName());
                String id = "set" + s.getName();

                innerClass.getBlock(id).writeLine(
                    "public Builder ", buildSetterName(s.getName()), "(", getQualifiedName(s), " ", vname, ") {"
                );

                if (arg.typeIsSource(s)) {
                    innerClass.getBlock(id).getBlock("body").writeLine(
                        "Composed.mapDto(", dtovar, ", ", vname, ");"
                    );
                } else {
                    innerClass.getBlock(id).getBlock("body").writeLine(
                        dtovar, ".", arg.getSetterNameOf(s, this), "(", vname, ");"
                    );
                }

                innerClass.getBlock(id).getBlock("body").writeLine("return this;");
                innerClass.getBlock(id).writeLine("}").endl();


            }

            innerClass.writeLine("}");
        }

        blockBuilder.writeLine("}");
        writeClass(arg, blockBuilder, imports, outputDir);

    }


    private void writeTransferToMethod(BlockBuilder builder, ARIDto dto, ARIClazz clazz, Set<String> imports) {
        String typeSign = getQualifiedName(clazz);
        String dtoName = dto.getName();
        String typeName = clazz.getName();
        String typeVarName = StringUtil.lowFirst(typeName);

        //TODO check if var is same as type??
        String blockId = dtoName + typeName;

        BlockBuilder toBlock = builder.getBlock("to" + blockId);

        this.onBeforeWritingMethod(toBlock, imports, false, null);
//        profileWriter.writeTransientAndJsonIgnoreAnnotations(toBlock, imports, false);
        toBlock.writeLine(
            "public final ", typeSign, " to", typeName, "() {"
        );

        BlockBuilder toBody = toBlock.getBlock("body");
        String constructor = "new " + typeName + "(";

        List<ARIProp> constructParameters = clazz.getConstructParameters(this);

        final Context self = this;
        if (!constructParameters.isEmpty()) {
            constructor += StringUtil.join(constructParameters, ",", new JoinIterator<ARIProp>() {
                @Override
                public String toString(ARIProp arg) {
                    Getter getter = new Getter(arg, self);
                    return "this." + getter.getName() + "()";
                }
            });
        }

        constructor += ")";

        toBody.writeLine(typeSign, " ", typeVarName, " = ", constructor, ";");

        toBody.writeLine("this", ".mapSource(", typeVarName, ", this);");

        toBody.writeLine("return ", typeVarName, ";");
        toBlock.writeLine("}").endl();
    }


    private String toVarName(String s){
        StringBuilder r = new StringBuilder();
        r.append(Character.toLowerCase(s.charAt(0)));
        boolean low = true;
        for (int i = 1; i < s.length(); i++){
            char c = s.charAt(i);
            if (Character.isLowerCase(c)){
                low = false;
            }
            if (low){
                r.append(Character.toLowerCase(c));
            } else {
                r.append(c);
            }
        }

        return r.toString();
    }

    @Override
    public boolean isPrimitive(ARIProp prop) {
        String typeName = prop.getTypeName();
        for (PrimitiveType t: PrimitiveType.values()){
            if (typeName.toLowerCase().equalsIgnoreCase(t.name().toLowerCase())){
                return true;
            }
        }
        String[] others = new String[]{"float", "byte", "Float", "Byte"};
        for (String s: others){
            if (typeName.equals(s)){
                return true;
            }
        }
        ARIType t = prop.getType(this);

        typeName = t.getName();
        if ("Map".equals(typeName)){
            return true;
        }

        return false;
    }

    private void writeTransferFromMethod(BlockBuilder builder, ARIDto dto, ARIClazz fromClazz) {

        String dtoSign = getQualifiedName(dto);
        String typeSign = getQualifiedName(fromClazz);

        String dtoName = dto.getName();
        String typeName = fromClazz.getName();

        String typeVarName = toVarName(typeName);
        String dtoVarName =toVarName(dtoName);

        //TODO check if var is same as type

        String blockId = dtoName + typeName;

        BlockBuilder mapBlock = builder.getBlock("mapDto" + blockId);

        mapBlock.writeLine("@SuppressWarnings(\"Duplicates\")");
        mapBlock.writeLine(
            "public static void mapDto(", dtoSign, " ", dtoVarName, ", ", typeSign, " ", typeVarName, ") {"
        );

        for (Setter s : fromClazz.setterList(this)) {
            Getter getter = dto.getTransferGetter(s, this);
            if (getter != null) {
                mapBlock.getBlock("body").writeLine(
                    dtoVarName, ".", s.getName(), "(", typeVarName, ".", getter.getName(), "());"
                );
            }

        }

        mapBlock.writeLine("}").endl();

        //source to dto static mapper:
        BlockBuilder mapSrc = builder.getBlock("mapSrc" + blockId);
        mapSrc.writeLine("@SuppressWarnings(\"Duplicates\")");
        mapSrc.writeLine(
            "public static void mapSource(", typeSign, " ", typeVarName, ", ", dtoSign, " ", dtoVarName, ") {"
        );
        for (Setter s : fromClazz.setterList(this)) {
            Getter getter = dto.getTransferGetter(s, this);
            if (getter != null) {
                mapSrc.getBlock("body").writeLine(typeVarName, ".", s.getName(), "(", dtoVarName, ".", getter.getName(), "());");
            }
        }
        mapSrc.writeLine("}").endl();

        //Static methods:
        BlockBuilder fromBlock = builder.getBlock("from " + blockId);

        fromBlock.writeLine(
            "public static ", dtoSign, " from", typeName,
            "(", typeSign, " ", typeVarName, ") {"
        );

        BlockBuilder fromBody = fromBlock.getBlock("body");

        fromBody.writeLine(
            dtoSign, " ", dtoVarName, " = new ", dtoSign, "();"
        );
        //write transfer in here

        fromBody.writeLine(
            dtoName, ".mapDto(", dtoVarName, ", ", typeVarName, ");"
        );

        fromBody.writeLine("return ", dtoVarName, ";");

        fromBlock.writeLine("}");
        fromBlock.endl();

    }


    private void writeEnum(ARINum e, File outputDir) {
        BlockBuilder blockBuilder = new BlockBuilder();

        blockBuilder.writeLine("public enum ", e.getName(), " {");
        String[] vars = e.getVariants();

        for (int i = 0; i < vars.length; i++) {
            blockBuilder.getBlock("variants").writeLine(
                vars[i],
                (i < vars.length - 1 ? "," : "")
            );
        }
        blockBuilder.writeLine("}");
        writeClass(e, blockBuilder, new HashSet<String>(), outputDir);
    }

    File getFile(ARIType t, File outputDir) {
        File outDir = getNamespace(t.getNamespaces(), outputDir);
        return new File(outDir.getAbsolutePath() + File.separator + t.getName() + ".java");
    }

    public File getNamespace(String[] names, File outputDir) {
        if (names == null || names.length == 0) {
            return outputDir;
        }
        String path = outputDir.getAbsolutePath() + File.separator + StringUtil.join(names, File.separator);
        System.out.println(path);
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    private String getAccess(AccessType accessType) {
        switch (accessType) {
            case PUBLIC:
                return "public";
            case PRIVATE:
                return "private";
            case PROTECTED:
                return "protected";
        }
        return "";
    }


    private void addImports(Collection<ARIType> types, ARIType current, final Set<String> imports) {

        for (ARIType type : types) {
            if (!"java.lang".equals(type.packId()) && !type.packId().isEmpty()
                && !current.packId().equals(type.packId())) {
                String importLine = ("import " + type.getFullyQualifiedNameJoinedBy("."));

                if (!imports.contains(importLine)) {
                    imports.add(importLine);
                }
            }
        }

        //write extra imports
        List<String> extraImports = this.filterLines(current, new FilterCriteria<String>() {
            @Override
            public boolean isAcceptable(String data) {
                return data.startsWith("import ") && !imports.contains(data);
            }
        });

        for (String s : extraImports) {
            if (!imports.contains(s)) {
                if (!s.endsWith(";")) {
                    s += ";";
                }
                imports.add(s);
            }
        }


    }


    private void compileClass(ARIClazz clazz, File outputDir) {
        if (clazz.isFinal() && clazz.isAbstract()) {
            throw new IllegalArgumentException("A class cannot be final and abstract in the same time in java at \n[" + clazz + "]");
        }

        if (AccessType.PRIVATE.equals(clazz.getAccess())) {
            throw new IllegalArgumentException("A class written in a separate file cannot be private \n[" + clazz + "]");
        }

        BlockBuilder blockBuilder = new BlockBuilder();

        Set<String> imports = new HashSet<>();

        addImports(clazz.getRequiredTypes(this), clazz, imports);

        //write comments if exists
        writeLinesAndComments(clazz, blockBuilder);

        this.onBeforeWritingClass(blockBuilder, clazz, imports);
      //  profileWriter.writeClassAnnotations(blockBuilder, clazz, imports);

        List<ARIType> implementedTypes = clazz.getImplementedTypes(this);

        if (clazz.isEmbeddable() && this.hasAnyOfTheProfiles(Support.PROFILE_HIBERNATE)){
            boolean containsSerializable = false;
            for (ARIType t: implementedTypes){
                if (t.getId().equals("java.io.Serializable")){
                    containsSerializable = true;
                    break;
                }
            }
            if (!containsSerializable){
                implementedTypes.add(new ARIType("Serializable", new String[]{"java", "io"}, false));
            }
        }

        addImports(implementedTypes, clazz, imports);
        Collections.sort(implementedTypes, new Comparator<ARIType>() {
            @Override
            public int compare(ARIType o1, ARIType o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });


        String access = getAccess(clazz.getAccess());
        //write class init
        blockBuilder.writeLine(
            !access.isEmpty() ? access + " " : "",
            clazz.isFinal() ? "final " : "",
            (clazz.isAbstract() && !clazz.isInteraface()) ? "abstract " : "",
            clazz.isInteraface() ? "interface " : "class ",
            getConstructName(clazz),
            " ",
            getExt("extends", clazz.getExtendedTypes(this)),
            getExt("implements", implementedTypes),
            "{"
        );


        this.onBeforeWritingProperties(blockBuilder, clazz, imports);

//        profileWriter.writeBeforePropDefinitions(blockBuilder, clazz, imports);

        //properties
        if (clazz.hasProperties()) {
            blockBuilder.endl();

            writeProps(blockBuilder, clazz.getFinalStaticProperties(AccessType.PUBLIC), clazz, imports);
            writeProps(blockBuilder, clazz.getFinalStaticProperties(AccessType.PROTECTED), clazz, imports);
            writeProps(blockBuilder, clazz.getFinalStaticProperties(AccessType.DEFAULT), clazz, imports);
            writeProps(blockBuilder, clazz.getFinalStaticProperties(AccessType.PRIVATE), clazz, imports);

            writeProps(blockBuilder, clazz.getFinalNonStaticProperties(AccessType.PUBLIC), clazz,  imports);
            writeProps(blockBuilder, clazz.getFinalNonStaticProperties(AccessType.PROTECTED), clazz,  imports);
            writeProps(blockBuilder, clazz.getFinalNonStaticProperties(AccessType.DEFAULT), clazz,  imports);
            writeProps(blockBuilder, clazz.getFinalNonStaticProperties(AccessType.PRIVATE),  clazz, imports);

            writeProps(blockBuilder, clazz.getNonFinalAndNonStaticProperties(AccessType.PUBLIC),  clazz, imports);
            writeProps(blockBuilder, clazz.getNonFinalAndNonStaticProperties(AccessType.PROTECTED), clazz,  imports);
            writeProps(blockBuilder, clazz.getNonFinalAndNonStaticProperties(AccessType.DEFAULT), clazz,  imports);
            writeProps(blockBuilder, clazz.getNonFinalAndNonStaticProperties(AccessType.PRIVATE),  clazz, imports);

        }

        //constructor
        if (!clazz.getClazzMode().equals(ClazzMode.INTERFACE) && clazz.needsConstructor(this)) {
            blockBuilder.endl();
            String joinedArgs;
            joinedArgs = StringUtil.join(clazz.getConstructParameters(this), ", ", new JoinIterator<ARIProp>() {
                @Override
                public String toString(ARIProp value) {
                    return getPropertyTypeName(value) + " " + value.getName();
                }
            });

            BlockBuilder constructor = blockBuilder.getBlock("constructor");

            constructor.header().writeLine("public ", clazz.getName(), "(", joinedArgs, ") {");
            constructor.footer().writeLine("}");

            List<ARIProp> inheritedFinals = clazz.getInheritedFinals(this);
            if (!inheritedFinals.isEmpty()) {
                joinedArgs = StringUtil.join(inheritedFinals, ", ", new JoinIterator<ARIProp>() {
                    @Override
                    public String toString(ARIProp value) {
                        return value.getName();
                    }
                });
                constructor.getBlock("super").writeLine("super(", joinedArgs, ");");
            }

            List<ARIProp> thisFinals = clazz.getFinalNonStaticProperties();
            for (int i = 0; i < thisFinals.size(); i++) {
                ARIProp p = thisFinals.get(i);
                constructor.getBlock(p.getName()).writeLine("this.", p.getName(), " = ", p.getName(), ";");
            }
        }

        List<ARIMethod> methods = clazz.gettersAndSettersList(this);

        writeGettersAndSetters(methods, blockBuilder, clazz, imports);

        blockBuilder.endl();
        blockBuilder.writeLine("}");

        writeClass(clazz, blockBuilder, imports, outputDir);


    }


    private void writeClass(ARIType t, BlockBuilder builder, Set<String> imports, File outputDir) {
        File file = getFile(t, outputDir);
        List<String> xxx = new ArrayList<>();
        for (String s : imports) {
            xxx.add(s);
        }
        Collections.sort(xxx);

        String content = "package " + StringUtil.join(t.getNamespaces(), ".") + ";\n\n";

        if (imports != null && !imports.isEmpty()) {
            content += StringUtil.join(xxx, ";\n") + ";\n\n";
        }

        content += builder.toString();

        writeStringToFile(file, content);

    }

    private void writeGettersAndSetters(List<ARIMethod> methods, BlockBuilder blockBuilder, ARIType parent, Set<String> imports) {

        if (!methods.isEmpty()) {
            blockBuilder.endl();

            for (ARIMethod m : methods) {
                BlockBuilder mbl = blockBuilder.getBlock(m.getName());
                ARIProp source = ((Sourced) m).source();

                this.onBeforeWritingGetSet(mbl, source, m, imports);
//                profileWriter.writeMethodAnnotations(mbl, source, m, imports);

                String srcVarName = source.getName();
                if (m.isGetter()){
                    mbl.writeLine("public ", getPropertyTypeName(source), " ", m.getName(), "() {");
                    mbl.getBlock("body1st").writeLine("return this.", srcVarName, ";");
                    mbl.writeLine("}");
                }
                else if (m.isSetter()){

                    boolean voidWritten = false;
                    //void setter
                    if (isEnabled(SETTER_VOID) || isEnabled(SETTER_MIXED)){
                        mbl.writeLine("public void ", m.getName(), "(", getPropertyTypeName(source), " ", srcVarName, ") {");
                        mbl.getBlock("body").writeLine("this.", srcVarName, " = ", srcVarName, ";");
                        mbl.writeLine("}");
                        voidWritten = true;
                    }

                    //chained setter
                    if (isEnabled(SETTER_CHAINED) || isEnabled(SETTER_MIXED) ){
                        String methodName;
                        if (voidWritten){
                            methodName = srcVarName;
                            mbl.endl();
                        } else {
                            methodName = m.getName();
                        }

                        mbl.writeLine("public ", getQualifiedName(parent), " ", methodName, "(", getPropertyTypeName(source), " ", srcVarName, ") {");
                        mbl.getBlock("body2nd")
                            .writeLine("this.", srcVarName, " = ", srcVarName, ";")
                            .writeLine("return this;");
                        mbl.writeLine("}");
                    }
                }

                blockBuilder.endl();
            }
        }

    }


    private void writeLinesAndComments(ARILined lined, BlockBuilder builder) {

        List<String> comments = lined.getComments();

        if (lined instanceof ARIProp && comments != null && !comments.isEmpty()) {
            System.out.println(lined);
        }

        if (comments != null && !comments.isEmpty()) {
            builder.writeLine("/**");
            for (String s : comments) {
                builder.writeLine(" * ", s);
            }
            builder.writeLine("**/");
        }

        //write annotations before class
        List<String> annotations = this.filterLines(lined, new FilterCriteria<String>() {
            @Override
            public boolean isAcceptable(String data) {
                return data.startsWith("@");
            }
        });

        if (annotations != null && !annotations.isEmpty()) {
            builder.writeLine(StringUtil.join(annotations, "\n"));
        }
    }


    private void writeProps(BlockBuilder blockBuilder, Collection<ARIProp> props, ARIType context, Set<String> imports){
        writeProps(blockBuilder, props, context, imports, "clazz");
    }

    private void writeProps(BlockBuilder blockBuilder, Collection<ARIProp> props, ARIType context, Set<String> imports, String mode) {
        if (props.isEmpty()) {
            return;
        }

        for (ARIProp prop : props) {
            BlockBuilder propBuilder = blockBuilder.getBlock(prop.getName());
            writeLinesAndComments(prop, propBuilder);
            String accessType = getAccess(prop.getAccessType());
//            profileWriter.writePropertyAnnotations(propBuilder, prop, context, imports, mode);
            this.onBeforeWritingProperty(propBuilder, prop, context, imports, mode);

            propBuilder.writeLine(
                !accessType.isEmpty() ? accessType + " " : "",
                prop.isStatic() ? "static " : "",
                prop.isFinal() ? "final " : "",
                prop.isVolatile() ? "volatile " : "",
                prop.isTranzient() ? "transient " : "",
                getPropertyTypeName(prop), " ",
                prop.getName(),
                ";"
            ).endl();
        }
    }


    private String getExt(String prefix, List<ARIType> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(" ");
        for (int i = 0; i < extensions.size(); i++) {
            ARIType extension = extensions.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(getQualifiedName(extension));
        }
        sb.append(" ");
        return sb.toString();
    }


    String getPropertyTypeName(ARIProp prop) {
        String type = getQualifiedName(prop.getType(this));
        if (prop.isArray()) {
            return type + "[]";
        }
        return type;
    }

    String getQualifiedName(ARIType cType) {
        StringBuilder sb = new StringBuilder();
        sb.append(cType.getName());
        if (cType.hasDiamonds()) {
            List<ARIType> diamondImpls = cType.getCurrentDiamondImplementations();
            if (!diamondImpls.isEmpty()) {
                sb.append("<");
                String joined = StringUtil.join(diamondImpls, ", ", new JoinIterator<ARIType>() {
                    @Override
                    public String toString(ARIType value) {
                        return value.getName();
                    }
                });
                sb.append(joined);
                sb.append(">");
            }
        }

        return sb.toString();
    }

    String getConstructName(ARIType cType) {
        StringBuilder sb = new StringBuilder();
        sb.append(cType.getName());
        if (cType.hasDiamonds()) {
            sb.append("<");
            String joined = StringUtil.join(cType.getDiamondNames(), ", ");
            sb.append(joined);
            sb.append(">");
        }
        return sb.toString();
    }
}
