package com.arise.cargo.impl.plugins;

import com.arise.cargo.Plugin;

public class JVMPlugins {

  public static class BlindMapper  extends Plugin {

    public BlindMapper() {
      super("blind-mapper");
    }

    @Override
    protected Worker buildWorker(String name) {
      return new JVMMapperWorker(name, this);
    }
  }


  public static class ProtobufMapper extends Plugin {

    public ProtobufMapper() {
      super("protobuf-blind-mapper");
    }

    @Override
    protected Worker buildWorker(String name) {
      return new JVMMapperWorker(name, this){


        @Override
        protected String getReturn(String varname) {
          return varname + ".build();";
        }

        @Override
        protected String createType(String name) {
          return name + ".Builder";
        }

        @Override
        protected String createNewInstance(String name) {
          return name + ".newBuilder();";
        }
      };
    }
  }
}
