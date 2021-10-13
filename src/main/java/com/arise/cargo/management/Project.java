package com.arise.cargo.management;

import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Project {

    final String name;
    private String forcedPlatform = null;
    Whisker whisker = new Whisker();

   final Mole log;

    List<Dependency> dependencies = new ArrayList<>();
    private File outputDir;

    public Project(String name) {
        this.name = name;
        log = Mole.getInstance("Project{" + name + "}");
    }

    public static Project fromMap(Map data) {
        Project project = new Project(MapUtil.getString(data, "name"));
        project.forcedPlatform = MapUtil.getString(data, "forced-platform");
        List<String> dependenciesNames = MapUtil.getList(data, "dependencies");

        for(String d: dependenciesNames){
            Dependency dependency = DependencyManager.getDependency(d);
            if (dependency == null){
                throw new RuntimeException("No dependency [" + d + "] found");
            }
            project.dependencies.add(dependency);
        }
        return project;
    }



    public void outputTo(File outputDir) throws Exception {
        this.outputDir = outputDir;
        List<DependencyManager.Resolution> resolutions = new ArrayList<>();
        for(Dependency dependency: dependencies){
            DependencyManager.Resolution resolution = DependencyManager.solveWithPlatform(dependency,
                    dependency.getVersion(forcedPlatform),
                    outputDir,
                    log);

            if (resolution != null){
                resolutions.add(resolution);
            }
        }

        Map<String, String> context = new HashMap<>();
        context.put("name", name);

        //TODO configurable
        File binDir = new File(outputDir, "bin");
        if(!binDir.exists()){
            binDir.mkdirs();
        }

        for (DependencyManager.Resolution resolution: resolutions){
            Dependency.Version version = resolution.selectedPlatform();
            String includes = StringUtil.join(version.includes, ";", new StringUtil.JoinIterator<String>() {
                @Override
                public String toString(String value) {
                    return new File(resolution.uncompressed(), value).getAbsolutePath();
                }
            });

            for (String staticLib: version.staticLibs){
                File dll = (new File(resolution.uncompressed(), staticLib));
                if (!dll.exists()){
                    throw new RuntimeException("Invalid zip file, wtf???");
                }
                //copiaza dll-urile required in bin
                File required = new File(binDir, dll.getName());
                //TODO check length
                if (!required.exists()){
                    Files.copy(dll.toPath(), new File(binDir, dll.getName()).toPath());
                }
            }

            context.put("includes-win64", includes);

            String linkDirectories = StringUtil.join(version.libPaths, ";", new StringUtil.JoinIterator<String>() {
                @Override
                public String toString(String value) {
                    return new File(resolution.uncompressed(), value).getAbsolutePath();
                }
            });
            context.put("lib-paths-win64", linkDirectories);
            System.out.println(version);
        }


        String content = whisker.findAndCompileStream("_cargo_/visual_studio_template.vcxproj", context);
        File out = new File(outputDir, name + ".vcxproj");
        FileUtil.writeStringToFile(out, content);


        String mainCpp = whisker.findAndCompileStream("_cargo_/main.cpp", new HashMap<>());

        FileUtil.writeStringToFile( new File(outputDir, "main.cpp"), mainCpp);



        System.out.println(resolutions);
    }





}
