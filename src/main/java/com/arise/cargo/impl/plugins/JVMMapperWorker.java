package com.arise.cargo.impl.plugins;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIMethod.Getter;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Cargo;
import com.arise.cargo.Context;
import com.arise.cargo.Plugin;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StringUtil;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JVMMapperWorker extends JVMPluginWorker {



  public JVMMapperWorker(String name, Plugin parent) {
    super(name, parent);
  }

  private String[] exceptions;

  @Override
  public void execute(Context context, File outDir) {

    String [] fullnamespace = extendNamespace(context, "mappers");

    BlockBuilder blockBuilder = new BlockBuilder();
    blockBuilder.writeLine("public final class ", name, " {").endl();

    Set<String> imports = new HashSet<>();

    BlockBuilder inner = blockBuilder.getBlock("content" + UUID.randomUUID());

    for (String[] args: getInstructions()){
      String fromClass = Cargo.stringAfter("from", args);

      String toClass = Cargo.stringAfter("to", args);

      exceptions = Cargo.stringsAfter("except", args);

      ARIType sourceType = findNullable(fromClass, context);
      if (sourceType == null){
        sourceType = findNullable(toClass, context);
      }

      if (sourceType == null){
        throw new LogicalException("Cannot blind map without any source class");
      }



      createMapper(inner, imports, sourceType, fromClass, toClass, context);
      inner.endl();

    }

    File out = getJavaFile(outDir, context, name, "mappers");


    blockBuilder.writeLine("}");

    String fullContent = "package " + StringUtil.join(fullnamespace, ".") + ";\n\n";
    fullContent += StringUtil.join(imports, "\n");
    fullContent += "\n\n" + blockBuilder.toString();

    FileUtil.writeStringToFile(out, fullContent);
  }


  private void createMapper(BlockBuilder blockBuilder, Set<String> imports, ARIType sourceType, String fromClass, String toClass, Context context) {
    ARIClazz clazz = (ARIClazz) sourceType;




    if (clazz.getNamespaces() != null){
      imports.add("import " + StringUtil.join(clazz.getNamespaces(), ".") + "." + clazz.getName() + ";" );
    }


    blockBuilder.writeLine("public static ", toClass, " map(", fromClass, " srcVal) {");



    BlockBuilder inner = blockBuilder.getBlock("inner" + UUID.randomUUID());
    inner.writeLine(createType(toClass), " retVal", " = ", createNewInstance(toClass));

    List<Getter> getters = clazz.getterList(context);

    for (Getter g: getters){
      if (isException(g)){
        continue;
      }
      ARIProp prop = g.source();
      String mn = g.getName();
      String setter = "s" + mn.substring(1, mn.length());

      if (prop.isPrimitive(context)){
        inner.writeLine("retVal", ".", setter, "(", "srcVal", ".", mn, "());");
      } else {
        inner.writeLine("retVal", ".", setter, "(",  "map(srcVal", ".", mn, "()));");
      }
    }


    inner.writeLine("return ", getReturn("retVal"));
    blockBuilder.writeLine("}");


    blockBuilder.endl();
    blockBuilder.writeLine("//", "public static ", toClass, " merge( ", toClass, " ret, ", fromClass, " src) {");

    String id = UUID.randomUUID().toString() + toClass;
    String body = "body" + id;
    blockBuilder.getBlock(body).writeLine("//", "if (ret == null) {");
    blockBuilder.getBlock(body).getBlock("bodyInIf" + id).writeLine("//", "ret = ", createNewInstance(toClass));
    blockBuilder.getBlock(body).writeLine("//", "}");


    blockBuilder.getBlock(body).writeLine("//return ", getReturn("retVal"));
    blockBuilder.writeLine("//", "}");
  }


  private boolean isException(Getter method){
    if (exceptions == null || exceptions.length == 0){
      return false;
    }
    for (String s: exceptions){
      if (method.source().getName().equals(s) || method.getName().equals(s)){
        return true;
      }
    }
    return false;
  }

  protected void writeMapping(BlockBuilder blockBuilder, String toClass, ARIClazz clazz, Context context, String srcVarName, String retValName){

    BlockBuilder inner = blockBuilder.getBlock("inner" + UUID.randomUUID());
    inner.writeLine(createType(toClass), srcVarName, " = ", createNewInstance(toClass));

    List<Getter> getters = clazz.getterList(context);

    for (Getter g: getters){
      ARIProp prop = g.source();

      if (prop.isPrimitive(context)){
        String mn = g.getName();
        String setter = "s" + mn.substring(1, mn.length());
        inner.writeLine(retValName, ".", setter, "(", srcVarName, ".", mn, "());");
      } else {
        inner.writeLine("//" + prop.getName(), "????");
      }


    }


  }

  protected String createType(String toClass) {
    return toClass;
  }


  protected String getReturn(String varname) {
    return varname + ";";
  }

  protected String createNewInstance(String name) {
    return "new " + name + "();";
  }
}
