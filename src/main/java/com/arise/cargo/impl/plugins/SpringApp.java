package com.arise.cargo.impl.plugins;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIDto;
import com.arise.cargo.Cargo;
import com.arise.cargo.Context;
import com.arise.cargo.Plugin;
import com.arise.cargo.Plugin.Worker;
import com.arise.cargo.impl.JVMWriter;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import java.io.File;
import java.util.logging.SimpleFormatter;

public class SpringApp extends Plugin {

  public SpringApp() {
    super("spring-app");
  }

  @Override
  protected Worker buildWorker(String name) {
    return new SpringWorker(name, this);
  }

  public static class SpringWorker extends JVMPluginWorker{

    public SpringWorker(String name, Plugin parent) {
      super(name, parent);
    }

    @Override
    public void execute(Context context, File output) {
      if (! (context instanceof JVMWriter) ){
        return;
      }

      for (String[] s: getInstructions()){
        if ("service".equals(s[0])){
          String serviceName = Cargo.stringAfter("service", s);
          String entityName = Cargo.stringAfter("for", s);
          String dtoName = Cargo.stringAfter("using", s);
          buildService(serviceName, entityName, dtoName, context, output);
        }
      }

    }

    private void buildService(String serviceName, String entityName, String dtoName, Context context, File output) {

      ARIClazz entity = (ARIClazz) context.getTypeByName(entityName);
      ARIDto dto = (ARIDto) context.getTypeByName(dtoName);


      File f = getJavaFile(output, context, serviceName, "services");


      String ext[] = extendNamespace(context, "services");

      BlockBuilder blockBuilder = new BlockBuilder();
      blockBuilder.writeLine("package ", StringUtil.join(ext, "."), ";\n\n");
      blockBuilder.writeLine("public interface " + serviceName + " {\n");


      blockBuilder.writeLine("}");


      FileUtil.writeStringToFile(f, blockBuilder.toString());
    }
  }


}
