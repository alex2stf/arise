package com.arise.cargo;

import com.arise.cargo.management.DependencyManager;
import com.arise.cargo.management.Project;
import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.ReflectUtil;

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

public class CargoMain {



    public static void main(String[] args) throws Exception {
//        String mode = args[0];
        DependencyManager.importDependencyRules("_cargo_/dependencies.json");


        String mainFile = args[0];


        File inputFile = new File(mainFile);

        Map data = (Map) Groot.decodeFile(inputFile);
        Project project = Project.fromMap(data);

        File f = new File("C:\\Applications\\cargo-deps");
        if (!f.exists()){
            f.mkdir();
        }

        project.outputTo(f, inputFile.getParentFile());

//        System.out.println(project);
    }
}
