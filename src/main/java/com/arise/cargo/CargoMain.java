package com.arise.cargo;

import com.arise.cargo.management.DependencyManager;
import com.arise.cargo.management.Project;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CargoMain {



    public static void main(String[] args) throws Exception {
//        String mode = args[0];

        Double.compare(1, 2);


        DependencyManager.importDependencyRules("_cargo_/dependencies.json");
        File inputFile = new File("C:\\Users\\alexandru2.stefan\\Documents\\om-automated-test\\c-tests\\c-tests.json");

        Map data = (Map) Groot.decodeFile(inputFile);

        Project project = Project.fromMap(data);

        project.outputTo(inputFile.getParentFile());
        System.out.println(project);
    }
}
