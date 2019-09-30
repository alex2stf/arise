package com.arise.cargo.impl.plugins;

import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.cargo.Plugin;
import com.arise.core.tools.FileUtil;
import java.io.File;

public abstract class JVMPluginWorker extends Plugin.Worker {

  public JVMPluginWorker(String name, Plugin parent) {
    super(name, parent);
  }

  protected ARIType findNullable(String name, Context context){
    try {
      return context.getTypeByName(name);
    }catch ( Exception e){
      return null;
    }
  }

  public File getJavaFile(File outDir, Context context, String fileName, String ... ext){
    String [] fullnamespace = extendNamespace(context, ext);
    File out = FileUtil.extendPack(outDir, fullnamespace);
    return new File(out.getAbsolutePath() + File.separator + fileName + ".java");
  }

  public String[] extendNamespace(Context context, String ... extend){
    int rootSize = 0;
    if (context.getRootNamespace() != null){
      rootSize = context.getRootNamespace().length;
    }

    String[] fullNamespace = new String[rootSize + extend.length];

    if (context.getRootNamespace() != null){
      for(int i = 0; i < rootSize; i++){
        fullNamespace[i] = context.getRootNamespace()[i];
      }
    }

    for (int i = 0; i < extend.length; i++){
      fullNamespace[i + rootSize] = extend[i];
    }
    return fullNamespace;
  }

}
