package com.arise.cargo;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.ReflectUtil;

import java.io.File;
import java.util.List;

public class CargoMain {



    public static void main(String[] args) throws Exception {
//        String mode = args[0];
        DependencyManager.importDependencyRules("_cargo_/dependencies.json");

        List<DependencyManager.Resolution> resolutions = DependencyManager.withDependencies(new String[]{
                "SELENIUM_JAR", "CHROME_DRIVER"
        });

        DependencyManager.Resolution chromeDriver = DependencyManager.findResolution(resolutions, "CHROME_DRIVER");


       System.setProperty("webdriver.chrome.driver", new File(chromeDriver.uncompressed(), chromeDriver.selectedVersion().getExecutable()).getAbsolutePath());
       Object driver = ReflectUtil.getClassByName("org.openqa.selenium.chrome.ChromeDriver").newInstance();
       ReflectUtil.getMethod(driver, "get", String.class).call("https://chromedriver.storage.googleapis.com/index.html?path=93.0.4577.63/");



//        File inputFile = new File("C:\\Users\\alexandru2.stefan\\Documents\\om-automated-test\\c-tests\\c-tests.json");
//
//        Map data = (Map) Groot.decodeFile(inputFile);
//
//        Project project = Project.fromMap(data);
//
//        project.outputTo(inputFile.getParentFile());
//        System.out.println(project);
    }
}
