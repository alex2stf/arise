package com.arise.canter;

import com.arise.core.tools.FilterCriteria;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.TypeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.StringUtil.QUOTE_ITERATOR;


public abstract class Command<T> extends Task<T> {
    protected String[] argumentsNames;

    protected final String id;
    protected Map<String, Object> properties;

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Command(String id){
        this.id = id;
    }


    @Override
    public String toString() {
        return
                StringUtil.jsonBuilder().objectStart()
                        .add("id", id)
                        .add("arg-names", argumentsNames, QUOTE_ITERATOR)
                        .add("properties", properties, QUOTE_ITERATOR)
                        .objectEnd().build();
    }

    public final Arguments buildArguments(String ... args) {
        Arguments arguments = new Arguments();
        if (args != null && args.length > 0){

            if(argumentsNames != null && argumentsNames.length > 0){
                int min = args.length < argumentsNames.length ? args.length : argumentsNames.length;
                for (int i = 0; i <min; i++){
                    arguments.put(argumentsNames[i], args[i]);
                }
            }

            for (String s: args){
                arguments.add(s);
            }
        }
        if (!isEmpty(executions)){
            batchExecute(executions, arguments, getRegistry().newAsyncExecutor());
        }
        if (!isEmpty(properties)){
            parseProps(properties, arguments, getFieldCriteria(), getMethodCriteria());
        }

        return arguments;
    }


    public static void parseProps(Map<String, Object> properties, Arguments arguments, final FilterCriteria<Field> fieldFilterCriteria, final FilterCriteria<Method> methodFilterCriteria){
        if (isEmpty(properties)){
            return;
        }

        for (Map.Entry<String, Object> e: properties.entrySet()){
            final Map<String, Object> mapargs = arguments.getMapArgs();
            if (e.getValue() instanceof String){
                String result = StringUtil.map(String.valueOf(e.getValue()), mapargs, fieldFilterCriteria, methodFilterCriteria);
                mapargs.put(e.getKey(), result);
                arguments.setMapArgs(mapargs);
            }
            else if (TypeUtil.isSingleKeyIterable(e.getValue())){
                final List<String> compiledItems = new ArrayList<>();
                TypeUtil.forEach(e.getValue(), new TypeUtil.IteratorHandler() {
                    @Override
                    public void found(Object key, Object value, int index) {
                        String compiled = StringUtil.map(String.valueOf(value), mapargs, fieldFilterCriteria, methodFilterCriteria);
                        compiledItems.add(compiled);
                    }
                }, true, methodFilterCriteria, fieldFilterCriteria);
                mapargs.put(e.getKey(), compiledItems);
                arguments.setMapArgs(mapargs);
            }
            else {
                Object parsed = TypeUtil.objectToMap(e.getValue(), true, new TypeUtil.IteratorConverter() {
                    @Override
                    public Object convert(Object key, Object value, int index) {
                        if (value instanceof String){
                            return StringUtil.map(String.valueOf(value), mapargs, fieldFilterCriteria, methodFilterCriteria);
                        }
                        return value;
                    }
                }, fieldFilterCriteria, methodFilterCriteria);
                mapargs.put(e.getKey(), parsed);
                arguments.setMapArgs(mapargs);
            }
        }
    }





    public final T execute(String ... args){
        Arguments arguments = buildArguments(args);
        return execute(arguments);
    };

    public abstract T execute(Arguments arguments);


    public String getId() {
        return id;
    }

    public Command setArgumentNames(String ... args) {
        this.argumentsNames = args;
        return this;
    }



    public Command<T> addProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }


    public Command<T> setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }


}
