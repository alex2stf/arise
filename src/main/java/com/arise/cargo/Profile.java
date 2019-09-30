package com.arise.cargo;

import com.arise.cargo.impl.profiles.CatatumboProfile;
import com.arise.cargo.impl.profiles.HibernateProfile;
import com.arise.cargo.impl.profiles.JPAProfile;
import com.arise.cargo.impl.profiles.JacksonProfile;
import com.arise.cargo.impl.profiles.MavenProfile;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.StringUtil;
import java.util.Set;

public abstract class Profile {

  private final String name;
  private final Context context;

  protected Profile(String name, Context context) {
    this.name = name;
    this.context = context;
  }

  protected void addImport(String n, Set<String> imports){
    imports.add(n);
  }

  public String getSafeColumnName(ARIClazz ariClazz){
    String columnName = StringUtil.toSnakeCase(ariClazz.getAlias());
    if (columnName == null){
      columnName = StringUtil.toSnakeCase(ariClazz.getName());
    }
    return columnName;
  }


//TODO single method
  public String getSafeColumnName(ARIProp prop){
    String columnName = StringUtil.toSnakeCase(prop.getAlias());
    if (columnName == null){
      columnName = StringUtil.toSnakeCase(prop.getName());
    }

    switch (columnName){
      case "date":
        return getSafeColumnName(prop.getParent()) + "_date";
    }

    return columnName;
  }


  public final Context getContext(){
    return context;
  }

  @Override
  public String toString() {
    return id();
  }

  @Override
  public final int hashCode() {
    return id().hashCode();
  }

  public String  id() {
    return  name;
  }

  public abstract void onBeforeWritingMethod(BlockBuilder builder, Set<String> imports, boolean persistable, ARIMethod method);

  public abstract void onBeforeWritingClass(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports);

  public abstract void onBeforeWritingProperties(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports);

  public abstract void onBeforeWritingProperty(BlockBuilder blockBuilder, ARIProp prop, ARIType context, Set<String> imports, String mode);

  public abstract void onBeforeWritingGetSet(BlockBuilder blockBuilder, ARIProp source, ARIMethod method, Set<String> imports);

  public static class DefaultFactory{
    public static Profile get(String s, Context context){
      switch (s){
        case "maven": return new MavenProfile(context);
        case "hibernate": return new HibernateProfile(context);
        case "catatumbo": return new CatatumboProfile(context);
        case "jackson": return new JacksonProfile(context);
        case "jpa": return new JPAProfile(context);
        case "javax": return new JPAProfile(context);
      }
      return null;
    }
  }
}
