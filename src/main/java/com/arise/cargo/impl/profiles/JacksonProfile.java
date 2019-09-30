package com.arise.cargo.impl.profiles;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIMethod;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.cargo.Profile;
import com.arise.core.tools.BlockBuilder;
import java.util.Set;

public class JacksonProfile extends Profile {

  public JacksonProfile(Context context) {
    super("jackson", context);
  }

  @Override
  public void onBeforeWritingMethod(BlockBuilder builder, Set<String> imports, boolean persistable, ARIMethod method) {
    addImport("import com.fasterxml.jackson.annotation.JsonIgnore", imports);
    builder.writeLine("@JsonIgnore");
  }

  @Override
  public void onBeforeWritingClass(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {

  }

  @Override
  public void onBeforeWritingProperties(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {

  }

  @Override
  public void onBeforeWritingProperty(BlockBuilder blockBuilder, ARIProp prop, ARIType context, Set<String> imports, String mode) {

  }

  @Override
  public void onBeforeWritingGetSet(BlockBuilder blockBuilder, ARIProp source, ARIMethod method, Set<String> imports) {

  }
}
