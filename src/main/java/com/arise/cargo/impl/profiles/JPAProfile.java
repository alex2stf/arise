package com.arise.cargo.impl.profiles;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIMethod;
import com.arise.cargo.ARINum;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIType;
import com.arise.cargo.Context;
import com.arise.cargo.Profile;
import com.arise.cargo.RelationType;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.StringUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JPAProfile extends Profile {

  JPAProfile(String name, Context context) {
    super(name, context);
  }

  public JPAProfile(Context context) {
    super("jpa", context);
  }

  @Override
  public void onBeforeWritingMethod(BlockBuilder builder, Set<String> imports, boolean persistable, ARIMethod method) {
    if (persistable) {
      addImport("import javax.persistence.Transient", imports);
      builder.writeLine("@Transient");
    }
  }

  private void writeAttributesOvveride(ARIProp prop, BlockBuilder blockBuilder, Set<String> imports){
    if (prop.hasAttributes()) {
      addImport("import javax.persistence.AttributeOverride", imports);
      addImport("import javax.persistence.AttributeOverrides", imports);
      addImport("import javax.persistence.Column", imports);

      blockBuilder.writeLine("@AttributeOverrides(value = {");
      int count = 0;
      for (Map.Entry<String, String> entry : prop.getAttributes().entrySet()) {
        String c = "";
        if (count < prop.getAttributes().size() - 1) {
          c = ",";
        }
        blockBuilder.getBlock("attibutes-ovveride").writeLine(
            "@AttributeOverride(name = \"" + entry.getKey() + "\", column = @Column(name = \"" + entry.getValue() + "\"))", c
        );
        count++;
      }
      blockBuilder.writeLine("})");
    }
  }

  @Override
  public void onBeforeWritingClass(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {
    //@Entity && @Table
    if (clazz.isPersistable() && !clazz.isEmbeddable()){
      addImport("import javax.persistence.Entity", imports);
      blockBuilder.writeLine("@Entity");

      addImport("import javax.persistence.Table", imports);
      blockBuilder.writeLine("@Table(name = \""+getSafeColumnName(clazz)+"\")");
    }
    else  if (clazz.isEmbeddable()){
      addImport("import javax.persistence.Embeddable", imports);
      blockBuilder.writeLine("@Embeddable");
    }
  }

  @Override
  public void onBeforeWritingProperties(BlockBuilder blockBuilder, ARIClazz clazz, Set<String> imports) {

  }

  @Override
  public void onBeforeWritingProperty(BlockBuilder blockBuilder, ARIProp prop, ARIType context, Set<String> imports, String mode) {
    if ("dto".equals(mode)){
      return;
    }

    if (prop.isTranzient()){
      addImport("import javax.persistence.Transient", imports);
      blockBuilder.writeLine("@Transient");
      return;
    }

    ARIType parentType = prop.getType(getContext());
    ARIClazz parentClass = null;
    ARINum parentEnum = null;
    boolean embeddedId = false;


    if (parentType instanceof ARIClazz){
      parentClass = (ARIClazz) parentType;
    }

    if (parentType instanceof ARINum){
      parentEnum = (ARINum) parentType;
    }




    //write embedded entity
    if (parentClass != null && parentClass.isEmbeddable()){
      if (prop.isPrimaryKey()){
        addImport("import javax.persistence.EmbeddedId", imports);
        blockBuilder.writeLine("@EmbeddedId");
        writeAttributesOvveride(prop, blockBuilder, imports);
        embeddedId = true;
      }
      else {
          addImport("import javax.persistence.Embedded", imports);
          blockBuilder.writeLine("@Embedded");
          writeAttributesOvveride(prop, blockBuilder, imports);
          //no need any other data after embedded ???
          return;
      }

    }

    if (prop.isPrimaryKey() && !embeddedId){
      writePrimaryKey(prop, blockBuilder, imports);
    }



    //write enum type
    if (parentEnum != null && prop.getParent().isPersistable()){
      addImport("import javax.persistence.Enumerated", imports);
      addImport("import javax.persistence.EnumType", imports);
      if (prop.hasAttribute("enumtype")){
        blockBuilder.writeLine("@Enumerated(EnumType."+prop.getAttribute("enumtype").toUpperCase() +")");
      } else {
        blockBuilder.writeLine("@Enumerated(EnumType.STRING)");
      }
    }



    //write column name
    if ( (prop.getParent().isPersistable() || prop.getParent().isEmbeddable()) ){

      if (!propIsSerializable(prop) && !prop.isPrimaryKey() && parentEnum == null){
        RelationType relationType = getRelationType(prop);
        switch (relationType){
          case ONE_TO_ONE:
            addImport("import javax.persistence.OneToOne", imports);
            writeRelation("@OneToOne", blockBuilder, prop, imports, context);
            break;
          case ONE_TO_MANY:
            addImport("import javax.persistence.OneToMany", imports);
            writeRelation("@OneToMany", blockBuilder, prop, imports,  context);
            break;
          case MANY_TO_ONE:
            addImport("import javax.persistence.ManyToOne", imports);
            writeRelation("@ManyToOne", blockBuilder, prop, imports,  context);
            break;

          default:
            blockBuilder.writeLine("//TODO relation and join column");
        }

        if (prop.hasAttribute("maps_id")){
          addImport("import javax.persistence.MapsId", imports);
          blockBuilder.writeLine("@MapsId(\"" + prop.getAttribute("maps_id") + "\")");
        }

      } else if(!embeddedId)  {
        String safeColumnName = getSafeColumnName(prop);
        addImport("import javax.persistence.Column", imports);
        Integer columnLength = prop.getMaxLength();
        String columnLengthStr = columnLength != null ? ", length = " + columnLength : "";
        String uniqueLine = prop.isUnique() ? ", unique = true" : "";
        String nullableLine = !prop.isNullable() ? ", nullable = false" : "";

        blockBuilder.writeLine("@Column(name = \"" + safeColumnName + "\"" + columnLengthStr + uniqueLine + nullableLine + ")");
      }



    }
  }



  private RelationType getRelationType(ARIProp prop){
    RelationType relationType = null;
    if (prop.hasAttribute("relation")){
      relationType = RelationType.fromString(prop.getAttribute("relation"));
    }
    if (relationType == null) {
      throw new LogicalException("A valid relation required for property " + prop.getName());
    }
    return relationType;
  }



  public  boolean propIsSerializable(ARIProp prop) {
    if (prop.isPrimitive(getContext())){
      return true;
    }
    ARIType ARIType = prop.getType(getContext());

    String className = ARIType.getName();
    if (ARIType.getNamespaces() != null && ARIType.getNamespaces().length > 0){
      className = StringUtil.join(ARIType.getNamespaces(), ".") + "." + ARIType.getName();
    }
    return  ReflectUtil.classNameExtends(className, Serializable.class);
  }

  private void writePrimaryKey(ARIProp prop, BlockBuilder blockBuilder, Set<String> imports){
      addImport("import javax.persistence.Id", imports);
      addImport("import javax.persistence.GeneratedValue", imports);
      addImport("import javax.persistence.GenerationType", imports);
      blockBuilder.writeLine("@Id");
      String generationType = prop.getAttribute("strategy");
      if (!StringUtil.hasContent(generationType)) {
        throw new LogicalException("primary key generation strategy required at " + prop.getName() + " inside " + prop.getParent().getName());
      }
      if ("SEQUENCE".equalsIgnoreCase(generationType)) {
        String sequenceGenerator = prop.getAttributeOrRand("sequenceGenerator");
        String sequenceName = prop.getAttributeOrRand("sequenceName");
        String allocationSize = prop.getAttribute("allocationSize");
        if (!StringUtil.hasContent(allocationSize)) {
          allocationSize = "50";
        }

        addImport("import javax.persistence.SequenceGenerator", imports);

        blockBuilder.writeLine("@GeneratedValue(strategy = GenerationType.", generationType.toUpperCase(),
            ", generator=", StringUtil.quote(sequenceGenerator), ")");

        blockBuilder.writeLine("@SequenceGenerator(name = ", StringUtil.quote(sequenceGenerator),
            ", sequenceName=", StringUtil.quote(sequenceName),
            ", allocationSize=", allocationSize, ")");


      } else {
        blockBuilder.writeLine("@GeneratedValue(strategy = GenerationType.", generationType.toUpperCase(), ")");
      }
  }


  private void writeRelation(String root, BlockBuilder builder, ARIProp prop, Set<String> imports, ARIType context){
    String mappedBy = prop.getAttribute("mapped_by");
    if (mappedBy == null){
      mappedBy = prop.getAttribute("mappedby");
    }
    if (mappedBy != null){
      mappedBy = StringUtil.putBetweenQuotes(mappedBy);
    }
    String cascade = prop.getAttribute("cascade");
    if (cascade != null){
      addImport("import javax.persistence.CascadeType", imports);
      cascade = "CascadeType." + cascade.toUpperCase();
    }


    String orphanRemoval = prop.getAttribute("orphanRemoval");
    String fetchType = prop.getAttribute("fetch");
    if (fetchType != null){
      addImport("import javax.persistence.FetchType", imports);
      fetchType = "FetchType." + fetchType.toUpperCase();
    }


    boolean hasBody = (mappedBy != null || cascade != null || orphanRemoval != null || fetchType != null);

    if(hasBody){
      List<String> inner = new ArrayList<>();
      wAnnProp(inner, "mappedBy", mappedBy);
      wAnnProp(inner, "cascade", cascade);
      wAnnProp(inner, "fetch", fetchType);
      wAnnProp(inner, "orphanRemoval", orphanRemoval);
      if (inner.size() == 1){
        builder.writeLine(root, "(", inner.get(0), ")");
      } else {
        builder.writeLine(root, "(");
        builder.getBlock("inner").joinLines(inner, ",");
        builder.writeLine(")");
      }
    } else {
      builder.writeLine(root);
    }

    String joinColumn = prop.getAttribute("join_column");
    boolean selfReferenced = prop.getType(getContext()).getId().equals(context.getId());
    if (selfReferenced && joinColumn == null){
      throw new LogicalException("A reference to self must specify a join column for " + prop.getParent().getId() + " inside " + context.getId());
    }

    if (joinColumn != null){
      addImport("import javax.persistence.JoinColumn", imports);
      List<String> inner = new ArrayList<>();
      wAnnProp(inner, "name", StringUtil.quote(joinColumn));
      if(!prop.isNullable()){
        wAnnProp(inner, "nullable", String.valueOf(prop.isNullable()));
      }
      builder.writeLine("@JoinColumn(", StringUtil.join(inner, ", "), ")");
    }


    String foreignKey = prop.getAttribute("foreign_key");

  }


  private void wAnnProp(List<String> lines, String key, String value){
    if (value != null){
      lines.add(key + " = " + value);
    }
  }


  @Override
  public void onBeforeWritingGetSet(BlockBuilder blockBuilder, ARIProp source, ARIMethod method, Set<String> imports) {

  }
}
