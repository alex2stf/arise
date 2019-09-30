package com.arise.cargo.impl.profiles;

import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.core.tools.BlockBuilder;
import java.util.Set;

public class HibernateProfile extends JPAProfile {

  public HibernateProfile(Context c) {
    super("hibernate", c);
  }


  @Override
  public void onBeforeWritingProperty(BlockBuilder blockBuilder, ARIProp prop, ARIType context, Set<String> imports, String mode) {
    if ("dto".equals(mode)){
      return;
    }
    if (prop.hasAttribute("naturalid") ){
      addImport("import org.hibernate.annotations.NaturalId", imports);
      blockBuilder.writeLine("@NaturalId");
    }

    super.onBeforeWritingProperty(blockBuilder, prop, context, imports, mode);
    //write hibernate type:
    if (prop.hasAttribute("hibernate_type") ){
      addImport("import org.hibernate.annotations.Type", imports);
      blockBuilder.writeLine("@Type(type = \"", prop.getAttribute("hibernate_type"), "\")");
    }
  }
}
