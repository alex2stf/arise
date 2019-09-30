package com.arise.cargo.impl.profiles;

import com.arise.cargo.Context;
import com.arise.cargo.impl.Dependency;
import com.arise.core.tools.BlockBuilder;
import com.arise.core.tools.FileUtil;
import java.io.File;

public class MavenProfile extends ProjectProfile {

  private String groupId;
  private String artifactId;
  private String version;

  public MavenProfile(Context c) {
    super("maven", c);
  }


  @Override
  public File redefineOutput(File output) {
    return FileUtil.extendPack(output, "src", "main", "java");
  }

  @Override
  public MavenProfile withDependency(Dependency dependency) {
    return (MavenProfile) super.withDependency(dependency);
  }

  @Override
  public void compilationDone(File output) {

    File pomXml = new File(output.getAbsolutePath() + File.separator + "pom.xml");
    FileUtil.writeStringToFile(pomXml, toString());
  }


  public String getGroupId() {
    return groupId;
  }

  public MavenProfile setGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public MavenProfile setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public MavenProfile setVersion(String version) {
    this.version = version;
    return this;
  }

  @Override
  public String toString() {
    BlockBuilder root = new BlockBuilder();
    root.writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");


    root.writeLine("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">");




    BlockBuilder body = root.getBlock("body");

    body.writeLine("<modelVersion>4.0.0</modelVersion>").endl();

    body.writeLine("<groupId>", groupId, "</groupId>")
        .writeLine("<artifactId>", artifactId, "</artifactId>")
        .writeLine("<version>"+version+"</version>");


    if (!getDependencies().isEmpty()){
      body.endl();
      body.writeLine("<dependencies>");

      for (Dependency dependency: getDependencies()){
        BlockBuilder dep = body.getBlock(dependency.uid());
        dep.writeLine("<dependency>");
        dep.getBlock("inner").writeLine("<groupId>"+dependency.groupId()+"</groupId>");
        dep.getBlock("inner").writeLine("<artifactId>"+dependency.artifactId()+"</artifactId>");
        dep.getBlock("inner").writeLine("<version>"+dependency.version()+"</version>");
        dep.writeLine("</dependency>");


      }

      body.writeLine("</dependencies>");
      body.endl();
    }

    root.writeLine("</project>");
    return root.toString();
  }
}
