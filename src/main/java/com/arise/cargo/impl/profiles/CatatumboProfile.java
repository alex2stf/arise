package com.arise.cargo.impl.profiles;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIMethod;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.cargo.Profile;
import com.arise.core.tools.BlockBuilder;
import java.util.Set;
import java.util.UUID;

public class CatatumboProfile extends Profile {

  public CatatumboProfile(Context c) {
    super("catatumbo", c);
  }

  @Override
  public void onBeforeWritingMethod(BlockBuilder builder, Set<String> imports, boolean persistable, ARIMethod method) {

  }

  @Override
  public void onBeforeWritingClass(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {
    if(clazz.isPersistable() && !clazz.isEmbeddable()) {
      addImport("import com.jmethods.catatumbo.Entity", imports);
      blockBuilder.writeLine("@Entity(kind=\"" + getSafeColumnName(clazz) + "\")");
    } else if (clazz.isEmbeddable()){
      addImport("import com.jmethods.catatumbo.Embeddable", imports);
      blockBuilder.writeLine("@Embeddable");
    }
  }

  @Override
  public void onBeforeWritingProperties(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {
    if (clazz.isPersistable() && !clazz.isEmbeddable()) {
      blockBuilder.endl();
      blockBuilder.getBlock(UUID.randomUUID().toString()).writeLine("public static final String KIND = \"" + getSafeColumnName(clazz) + "\";");
    }
  }

  @Override
  public void onBeforeWritingProperty(BlockBuilder blockBuilder, ARIProp prop, ARIType rootType, Set<String> imports, String mode) {
    if ("dto".equals(mode)){
      return;
    }

    if (prop.isPrimaryKey()){
      addImport("import com.jmethods.catatumbo.Identifier", imports);
      blockBuilder.writeLine("@Identifier");
    } else {
      addImport("import com.jmethods.catatumbo.Property", imports);
      blockBuilder.writeLine("@Property(indexed = false)");
    }

    ARIType parentType = prop.getType(getContext());
    ARIClazz parentClass = null;

    if (parentType instanceof ARIClazz){
      parentClass = (ARIClazz) parentType;
    }

    if (parentClass != null && parentClass.isEmbeddable()){
      addImport("import com.jmethods.catatumbo.Embedded", imports);
      addImport("import com.jmethods.catatumbo.Imploded", imports);
      blockBuilder.writeLine("@Embedded");
      blockBuilder.writeLine("@Imploded");
    }



  }

  @Override
  public void onBeforeWritingGetSet(BlockBuilder blockBuilder, ARIProp source, ARIMethod method, Set<String> imports) {

  }
}
