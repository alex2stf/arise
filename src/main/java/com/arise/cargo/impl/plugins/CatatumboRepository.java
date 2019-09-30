package com.arise.cargo.impl.plugins;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIProp;
import com.arise.cargo.Cargo;
import com.arise.cargo.Context;
import com.arise.cargo.Plugin;
import com.arise.cargo.impl.JVMWriter;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StringUtil;
import java.io.File;

public class CatatumboRepository extends Plugin {

  public CatatumboRepository() {
    super("catatumbo-repository");
  }

  @Override
  protected Worker buildWorker(String name) {
    return new CatatumboRepositoryWorker(name, this);
  }


  public static class CatatumboRepositoryWorker extends JVMPluginWorker {

    public CatatumboRepositoryWorker(String name, Plugin parent) {
      super(name, parent);
    }

    @Override
    public void execute(Context context, File output) {
      for (String[] args: getInstructions()){
        String className = Cargo.stringAfter("for", args);
        if (className != null){
          generateRepo(className, context, output);
        }
      }
    }


    private void generateRepo(String className, Context context, File outputRoot){
      if (! (context instanceof JVMWriter) ){
        return;
      }

      ARIClazz clazz = (ARIClazz) context.getTypeByName(className);
      if(clazz == null || !clazz.isPersistable()){
        return;
      }



      String [] fullnamespace = extendNamespace(context, "catatumbo", "impl");

      File out = getJavaFile(outputRoot, context, name, "catatumbo", "impl");

      BlockBuilder blockBuilder = new BlockBuilder();
      blockBuilder.writeLine("package ", StringUtil.join(fullnamespace, "."), ";");

      blockBuilder.endl();

      if (clazz.getNamespaces() != null){
        blockBuilder.writeLine("import ", StringUtil.join(clazz.getNamespaces(), "."), ".", className, ";");
      }

      blockBuilder.writeLine("import com.jmethods.catatumbo.impl.DefaultEntityManager;");




      blockBuilder.endl();

      blockBuilder.writeLine("public class " + name + " {");
      blockBuilder.endl();

      blockBuilder.getBlock("finprosp").writeLine("private final DefaultEntityManager em;");
      blockBuilder.endl();

      BlockBuilder classBody = blockBuilder.getBlock("class-body");
      classBody.writeLine("public " + name + "(final DefaultEntityManager em) {");
      classBody.getBlock("construct").writeLine("this.em = em;");
      classBody.writeLine("}");


      for (ARIProp prop: clazz.getProperties()) {

        if(prop.isPrimaryKey()){
          classBody.writeLine("//public " + className + " findById(" + prop.getType(context) + " id) {");

        } else {
          classBody.writeLine("//findBy" + StringUtil.capFirst(prop.getName()));
        }
      }

      blockBuilder.writeLine("}");


      FileUtil.writeStringToFile(out, blockBuilder.toString());

      System.out.println(clazz);
    }
  }




}
