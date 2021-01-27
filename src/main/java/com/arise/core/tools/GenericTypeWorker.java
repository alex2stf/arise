package com.arise.core.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class GenericTypeWorker {
    FilterCriteria<Field> fieldCriteria = TypeUtil.defaultFieldFiltering;
    FilterCriteria<Method> methodCriteria = TypeUtil.getterMethodFiltering;

    public FilterCriteria<Field> getFieldCriteria() {
        return  fieldCriteria;
    }

    public FilterCriteria<Method> getMethodCriteria() {
        return methodCriteria;
    }

    public void setFieldCriteria(FilterCriteria<Field> fieldCriteria) {
        this.fieldCriteria = fieldCriteria;
    }

    public void setMethodCriteria(FilterCriteria<Method> methodCriteria) {
        this.methodCriteria = methodCriteria;
    }
}
