package com.arise.cargo;

import com.arise.core.tools.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ARILined {
    private List<String> lines;
    private List<String> comments;
    private Map<String, String> attributes = new HashMap<>();







    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> p) {
        this.lines = new ArrayList<>();
        for (String s: p){
            lines.add(s);
        }
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> p) {
        this.comments = new ArrayList<>();
        for (String s: p){
            comments.add(s);
        }
    }


    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String attributeName){
        return attributes.get(attributeName);
    }

    public String getAttributeOrRand(String attributeName){
        if (!StringUtil.hasContent(attributes.get(attributeName))){
            return UUID.randomUUID().toString();
        }
        return attributes.get(attributeName);
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttributes(){
        return !attributes.isEmpty();
    }

    public void setAttributes(String[] attributes) {
        for (String s: attributes){
            String parts[] = s.split("=");
            if (parts.length == 1){
                this.attributes.put(s, "true");
            } else {
                this.attributes.put(parts[0], parts[1]);
            }
        }
    }

    public boolean hasAttribute(String val) {
        return attributes.containsKey(val);
    }



}
