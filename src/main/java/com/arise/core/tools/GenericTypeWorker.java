package com.arise.core.tools;

import com.arise.core.models.FilterCriteria;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Deprecated
public abstract class GenericTypeWorker {
    FilterCriteria<Field> fieldCriteria = TypeUtil.defaultFieldFiltering;
    FilterCriteria<Method> methodCriteria = TypeUtil.getterMethodFiltering;

    public FilterCriteria<Field> getFieldCriteria() {
        return  fieldCriteria;
    }

    public FilterCriteria<Method> getMethodCriteria() {
        return methodCriteria;
    }

    public void setMethodCriteria(FilterCriteria<Method> methodCriteria) {
        this.methodCriteria = methodCriteria;
    }
}
