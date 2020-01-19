package com.arise.cargo.contexts;

import com.arise.cargo.Context;
import com.arise.cargo.model.AccessType;
import com.arise.cargo.model.CGContextAware;

public abstract class StrongTypedContext extends Context {
    protected StrongTypedContext(String id) {
        super(id);
    }

    @Override
    public String solveAccessType(CGContextAware cgClass) {
        if (cgClass.getAccessType().equals(AccessType.DEFAULT)){
            return null;
        }
        return cgClass.getAccessType().name().toLowerCase();
    }
}
