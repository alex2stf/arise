package com.arise.cargo.impl.profiles;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIMethod;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.cargo.Profile;
import com.arise.cargo.impl.Dependency;
import com.arise.core.tools.BlockBuilder;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class ProjectProfile extends Profile {

  private String projectName;

  private Set<Dependency> dependencies = new HashSet<>();

  protected ProjectProfile(String name, Context context) {
    super(name, context);
  }

  public Set<Dependency> getDependencies() {
    return dependencies;
  }

  @Override
  public final void onBeforeWritingMethod(BlockBuilder builder, Set<String> imports, boolean persistable, ARIMethod method) {

  }

  @Override
  public final void onBeforeWritingClass(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {

  }

  @Override
  public final void onBeforeWritingProperties(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {

  }

  @Override
  public final  void onBeforeWritingProperty(BlockBuilder blockBuilder, ARIProp prop, ARIType context, Set<String> imports, String mode) {

  }

  @Override
  public final  void onBeforeWritingGetSet(BlockBuilder blockBuilder, ARIProp source, ARIMethod method, Set<String> imports) {

  }

  public abstract File redefineOutput(File output);

  public ProjectProfile withDependency(Dependency dependency){
    dependencies.add(dependency);
    return this;
  }

  public abstract void compilationDone(File output);
}
