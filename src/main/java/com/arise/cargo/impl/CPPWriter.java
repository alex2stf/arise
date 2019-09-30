package com.arise.cargo.impl;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIMethod;
import com.arise.cargo.ARIMethod.Arg;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.LineBuilder;
import com.arise.cargo.AccessType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.StringUtil.JoinIterator;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPPWriter extends Context {

    public CPPWriter() {
        super("cpp");
    }


    @Override
    protected void globalTypeRegistered(ARIType type) {
        System.out.println("CPP REGISTER global " + type);

    }

    @Override
    public boolean isPrimitive(ARIProp prop) {
        return false;
    }


    Map<String, String[]> uniqueNamespaces = new HashMap<>();

    Map<String, BlockBuilder> blockBuilderMap = new HashMap<>();



    String getHeader(){
        String header = getLibName().toUpperCase();
        BlockBuilder blockBuilder = new BlockBuilder("", "    ");

        blockBuilder.header().writeLine("#ifndef ", header, "_H_1")
                             .writeLine("#DEFINE ", header, "_H_1");

        blockBuilder.endl();
        for (String s: this.getUniqueRequirements()){
            blockBuilder.writeLine("#include <", s, ">");
        }
        blockBuilder.endl();



        blockBuilder.footer().writeLine("#endif");





        for(ARIClazz c: getClasses().values()) {
            if (!uniqueNamespaces.containsKey(c.namespaceId())){
                uniqueNamespaces.put(c.namespaceId(), c.getNamespaces());
            }
        }

        for (Map.Entry<String, String[]> entry: uniqueNamespaces.entrySet()){
            String[] names = entry.getValue();
            blockBuilder.writeBlockNodes(names);

            for (int i = 0; i < names.length; i++){
                BlockBuilder cBlock = blockBuilder.getBlockNodeAt(i, names);
                LineBuilder cHeader = cBlock.header();
                if (!cHeader.isStarted()){
                    cHeader.writeLine("namespace ", names[i]).writeLine("{");
                    cBlock.footer().writeLine("}").writeLine("");
                }

            }
        }

        for(ARIClazz c: getClasses().values()) {
            BlockBuilder classBlock = blockBuilder.getBlockNode(c.getNamespaces()).getBlock("-class " + c.getName());
            classBlock.header().writeLine("class ", c.getName()).writeLine("{");



            if (c.hasProps(AccessType.DEFAULT)){
                writeProps(c.getByAccessType(AccessType.DEFAULT), classBlock.getBlock("default") );
            }

            if (c.hasProps(AccessType.PRIVATE)){
                writeProps(c.getByAccessType(AccessType.PRIVATE), classBlock.getBlock("private").writeLine("private:") );
            }

            if (c.hasProps(AccessType.PROTECTED)){
                writeProps(c.getByAccessType(AccessType.PROTECTED), classBlock.getBlock("protected").writeLine("protected:") );
            }

            if (c.hasProps(AccessType.PUBLIC)){
                writeProps(c.getByAccessType(AccessType.PUBLIC), classBlock.getBlock("public").writeLine("public:") );
            }

            if (c.hasPropBasedMethods(this) && !classBlock.blockExists("public")){
                classBlock.getBlock("public").writeLine("public:");
            }


            writeHeaderGettersAndSetters(c.setterList(this), classBlock.getBlock("public"));
            writeHeaderGettersAndSetters(c.getterList(this), classBlock.getBlock("public"));
            classBlock.footer().writeLine("}").endl();

        }


        return blockBuilder.toString();
    }

    private  <T extends ARIMethod> void writeHeaderGettersAndSetters(List<T> methods, BlockBuilder pubBlock) {
        if (methods.isEmpty()){
            return;
        }
        pubBlock.getBlock("props").endl();
        for (T m: methods){
            pubBlock.getBlock("props").writeLine(
                getTypeName(m.getReturnType(this)),
                " ",
                m.getName(),
                "(",
                StringUtil.join(m.getArguments(this), ", ", new JoinIterator<Arg>() {
                    @Override
                    public String toString(Arg value) {
                        return getTypeName(value.getType()) + " " + value.getName();
                    }
                }),
                ");"
            );
        }
    }


    private void writeProps(List<ARIProp> props, BlockBuilder block){
        for (ARIProp prop: props){
            block.getBlock("props").writeLine(getPropName(prop), " ", prop.getName(), ";");
        }
    }


    String getPropName(ARIProp prop){
        ARIType t = prop.getType(this);
        return getTypeName(t);
    }

    String getTypeName(ARIType t){
        if (t.getNamespaces() != null){
            return StringUtil.join(t.getNamespaces(), "::") + "::" + t.getName();
        }
        return t.getName();
    }


    @Override
    public void compile() {
        System.out.println("START");
        FileUtil.writeStringToFile(getHeaderFile(), getHeader());

        for (ARIClazz ARIClazz : getClasses().values()){

        }
    }




    File getHeaderFile() {
        return new File(getOutput().getAbsolutePath() + File.separator + getLibName().toLowerCase() + ".h");
    }

    File getFile(ARIType t) {
        return new File(getOutput().getAbsolutePath() + File.separator + t.getName() + ".cpp");
    }


}
