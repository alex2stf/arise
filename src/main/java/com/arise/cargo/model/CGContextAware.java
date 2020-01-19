package com.arise.cargo.model;

import com.arise.cargo.Context;

import java.util.ArrayList;
import java.util.List;

public class CGContextAware extends CGParamType {
    protected Context parentContext;
    protected  AccessType accessType;
    private List<String> commentLines;
    private List<String> commentBlocks;

    public String ctxAccessType(){
        if (parentContext == null){
            throw new RuntimeException("NO PARENT CONTEXT");
        }
        return parentContext.solveAccessType(this);
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public CGContextAware setAccessType(AccessType accessType) {
        this.accessType = accessType;
        return this;
    }

    public CGContextAware setParentContext(Context parentContext) {
        this.parentContext = parentContext;
        return this;
    }

    public CGContextAware setCommentLines(List<String> arg) {
        this.commentLines = new ArrayList<>();
        for (String s: arg){
            commentLines.add(s);
        }
        return this;
    }

    public CGContextAware setCommentBlocks(List<String> arg) {
        this.commentBlocks = new ArrayList<>();
        for (String s: arg){
            commentBlocks.add(s);
        }
        return this;
    }

    public List<String> getCommentLines() {
        return commentLines;
    }

    public List<String> getCommentBlocks() {
        return commentBlocks;
    }
}
