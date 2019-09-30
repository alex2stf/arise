package com.arise.cargo.impl;

public class Dependency {

  private final String gId;
  private final String aId;
  private final String vrs;

  public static final Dependency JAVA_JACKSON_CORE_297 = new Dependency("com.fasterxml.jackson.core", "jackson-annotations", "2.9.7");
  public static final Dependency HIBERNATE_536_FINAL = new Dependency("org.hibernate", "hibernate-core", "5.3.6.Final");

  public Dependency(String groupId, String artifactId, String version) {
    this.gId = groupId;
    this.aId = artifactId;
    this.vrs = version;
  }

  public String groupId() {
    return gId;
  }

  public String artifactId() {
    return aId;
  }

  public String version() {
    return vrs;
  }

  public String uid(){
    return (String.valueOf(gId) + String.valueOf(aId) + String.valueOf(vrs));
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Dependency){
      Dependency t = (Dependency) o;
      return uid().equals(t.uid());
    }
    return super.equals(o);
  }

  @Override
  public String toString() {
    return uid();
  }

  @Override
  public int hashCode() {
    return uid().hashCode();
  }
}
