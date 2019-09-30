package com.arise.cargo;

import com.arise.cargo.Context.Option;
import com.arise.cargo.impl.Dependency;
import com.arise.cargo.impl.JVMWriter;
import com.arise.cargo.impl.plugins.CatatumboRepository;
import com.arise.cargo.impl.plugins.JVMPlugins.BlindMapper;
import com.arise.cargo.impl.plugins.JVMPlugins.ProtobufMapper;
import com.arise.cargo.impl.plugins.SpringApp;
import com.arise.cargo.impl.profiles.MavenProfile;
import com.arise.core.tools.FileUtil;

import java.io.File;
import org.junit.Test;

public class CargoTest {




    public Cargo getFullInstance(){
        File out = FileUtil.findOrCreateUserPackage("arise-local", "jentil");




        Context jvmContext = new JVMWriter().withProfiles("hibernate", "jackson")
            .setOption(Option.SETTER_CHAINED, true)
            .setOutput(out.getAbsolutePath());

      MavenProfile mavenProfile = new MavenProfile(jvmContext)
          .setArtifactId("jentil.autogen")
          .setGroupId("arise.tests")
          .setVersion("0.1")
          .withDependency(Dependency.JAVA_JACKSON_CORE_297)
          .withDependency(Dependency.HIBERNATE_536_FINAL);
      jvmContext.addProfile(mavenProfile);


        return new Cargo()
            .addContext(jvmContext)
            .addPlugin(new SpringApp())
//            .addContext(new SQLScriptWriter("sql-ora", new ORASyntax()).setOutput(mainJava.getAbsolutePath()))
//            .addContext(new CPPWriter().setLibName("mydefs").setOutput("src/test/cpp") )
            .loadBasicTypes();
    }


    @Test
    public void testCompileDatastoreClasses(){
        File out = FileUtil.findOrCreateUserPackage("arise-local", "datastore");

        Context jvmContext =  new JVMWriter()
            .setOption(Option.SETTER_VOID, true)
            .withProfiles("spring", "services", "jackson", "catatumbo")
            .setProjectName("autogen")
            .setOutput(out);

      MavenProfile mavenProfile = new MavenProfile(jvmContext)
          .withDependency(Dependency.JAVA_JACKSON_CORE_297)
          .withDependency(new Dependency("com.jmethods", "catatumbo", "2.7.0"))
          .withDependency(new Dependency("com.readyup", "schema", "0.0.101"));
      jvmContext.addProfile(mavenProfile);

        Cargo cargo = new Cargo().addContext(jvmContext)
            .addPlugin(new ProtobufMapper())
            .addPlugin(new BlindMapper())
            .addPlugin(new CatatumboRepository())
            .loadBasicTypes()
            .readFromFile("src/test/resources/_cargo_/catacumbo_test.cargo")
            .compile();

    }



    @Test
    public void runTestHibernate() {
        getFullInstance()
            .readFromFile("src/test/resources/_cargo_/hibernate_sample.cargo")
            .compile();
    }
}