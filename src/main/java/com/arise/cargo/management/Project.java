package com.arise.cargo.management;

import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arise.core.tools.CollectionUtil.isEmpty;


public class Project {

    final String name;
    private String forcedPlatform = null;
    static final Whisker whisker = new Whisker();

    final Mole log;

    List<Dependency> dependencies = new ArrayList<>();

    private List includes;
    private List sources;

    public Project(String name) {
        this.name = name;
        log = Mole.getInstance("Project{" + name + "}");
    }

    public static Project fromMap(Map data) {
        Project project = new Project(MapUtil.getString(data, "name"));
        project.forcedPlatform = MapUtil.getString(data, "forced-platform");
        project.includes = MapUtil.getList(data, "include");
        project.sources = MapUtil.getList(data, "sources");
        List<String> dependenciesNames = MapUtil.getList(data, "dependencies");

        for (String d : dependenciesNames) {
            Dependency dependency = DependencyManager.getDependency(d);
            if (dependency == null) {
                throw new RuntimeException("No dependency [" + d + "] found");
            }
            project.dependencies.add(dependency);
        }
        return project;
    }


    private void append(Map<String, Set<String>> buf, String key, List<String> value, DependencyManager.Resolution r){
        if (!buf.containsKey(key)){
            buf.put(key, new HashSet<String>());
        }
        Set<String> exiting = buf.get(key);
        for (String s: value){
            exiting.add(
                    new File(r.uncompressed(), s).getAbsolutePath()
            );
        }
    }


    private boolean isAcceptedAsSource(String fname){
        //TODO configurabil
         return fname.endsWith(".cpp") || fname.endsWith(".h") || fname.endsWith(".c") || fname.endsWith(".hpp");
    }


    private File getSourceFile(String in, File projDir){
        File f = new File(in);
        if (!f.exists()){
            f = new File(projDir, in);
        }
        return f;
    }

    public void outputTo(File depsDir, File projDir) throws Exception {
        List<DependencyManager.Resolution> resolutions = new ArrayList<>();
        for (Dependency dependency : dependencies) {
            for (Dependency.Version version: dependency.getVersions().values()){
                DependencyManager.Resolution resolution = DependencyManager.solveWithPlatform(dependency,
                        version,
                        depsDir,
                        log);

                if (resolution != null) {
                    resolutions.add(resolution);
                }
            }
        }

        Map<String, String> context = new HashMap<>();
        context.put("name", name);




        Map<String, Set<String>> buf = new HashMap<>();

        for (DependencyManager.Resolution res : resolutions) {
            Dependency.Version version = res.selectedVersion();
            String includeId = "include-" + version.getPlatformMatch().toLowerCase();
            String libPathId = "lib-paths-" + version.getPlatformMatch().toLowerCase();
            append(buf, includeId, version.includes, res);
            append(buf, libPathId, version.libPaths, res);

            for (String staticLib : version.staticLibs) {
                File dll = (new File(res.uncompressed(), staticLib));
                if (!dll.exists()) {
                    throw new RuntimeException("Invalid zip file, wtf??? whereis " + dll.getAbsolutePath() + " ???");
                }
                File binDir = new File(projDir, "bin" + File.separator + version.getName().toLowerCase() );
                if (!binDir.exists()) {
                    binDir.mkdirs();
                }



                //copiaza dll-urile required in bin
                File required = new File(binDir, dll.getName());
                //TODO check length
                if (!required.exists()) {
                    Files.copy(dll.toPath(), new File(binDir, dll.getName()).toPath());
                }
            }
        }

        for (Map.Entry<String, Set<String>> e: buf.entrySet()){
            String id = "vc-" + e.getKey();
            log.trace("defined key", id);
            context.put(id, StringUtil.join(e.getValue(), ";"));
        }

        if (!isEmpty(includes)){
            context.put("vc-project-includes", StringUtil.join(includes, ";", new StringUtil.JoinIterator<Object>() {
                @Override
                public String toString(Object v) {
                    File f = new File("" + v);
                    return f.getPath();
                }
            }));
        } else {
            context.put("vc-project-includes", "");
        }

        if (!isEmpty(sources)){
           List<String> files = new ArrayList<>();
           for (Object src: sources){
               File f = getSourceFile(src + "", projDir);
               if (!f.isDirectory() && isAcceptedAsSource(f.getName())){
                   sources.add(f.getAbsolutePath());
               }
               else if (f.isDirectory()){
                   File [] innerFiles = f.listFiles(new FilenameFilter() {
                       @Override
                       public boolean accept(File dir, String name) {
                          return isAcceptedAsSource(name);
                       }
                   });
                   if (null != innerFiles){
                       for (File z : innerFiles){
                           files.add(z.getAbsolutePath());
                       }
                   }
               }
           }
            context.put("vc-cl-compile", StringUtil.join(files, "\n", new StringUtil.JoinIterator<String>() {
                @Override
                public String toString(String value) {
                    if (value.endsWith(".h") || value.endsWith(".hpp") ){
                        return "    <ClInclude Include=\""+value+"\" />";
                    }
                    return "    <ClCompile Include=\""+value+"\" />";
                }
            }));

        } else {
            context.put("vc-cl-compile", "");
        }

        context.put("project-name", name);


        File out = new File(projDir, name + ".vcxproj");

        String content = whisker.findAndCompileStream("_cargo_/visual_studio_template.vcxproj", context);
        FileUtil.writeStringToFile(out, content);





        File mainFile = new File(projDir, "main.cpp");
        if(!mainFile.exists()){
            String mainCpp = whisker.findAndCompileStream("_cargo_/main.cpp", new HashMap<>());
            FileUtil.writeStringToFile(new File(projDir, "main.cpp"), mainCpp);
        }



        System.out.println(resolutions);
    }


}
