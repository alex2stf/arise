package com.arise.cargo.management;

import com.arise.core.tools.SYSUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Dependency {
    List<Rule> ruleList = new ArrayList<>();
    private String name;


    public Dependency addRule(Rule rule) {
        ruleList.add(rule);
        return this;
    }


    public Dependency setWindowsSource(final String path){
        return addRule(new Rule() {
            @Override
            public boolean acceptConditions() {
                return SYSUtils.isWindows();
            }

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String getName() {
                return "win32";
            }
        });
    }

   public Dependency setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }
}
