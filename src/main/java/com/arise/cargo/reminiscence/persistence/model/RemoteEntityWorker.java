package com.arise.cargo.reminiscence.persistence.model;

import com.arise.cargo.ARIClazz;
import com.arise.cargo.ARIProp;
import com.arise.cargo.ARIValue;
import com.arise.core.tools.Mole;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * define basic methods to be used when working with a remote server, no matter the communication protocol (DB or HTTP api)
 */
public abstract class RemoteEntityWorker<R> {
    private static final Mole log = Mole.getInstance(RemoteEntityWorker.class);

    /**
     * should return true if defined class exists
     * @param table
     * @return
     */
    public abstract boolean classExists(ARIClazz table);

    /**
     * should return true if defined property exists into target system
     * @param column
     * @return
     */
    protected abstract boolean propertyExists(ARIProp column);
    protected abstract boolean createClass(ARIClazz table);
    protected abstract void createProperty(ARIProp column);
    protected abstract void dropProperty(ARIProp column);
    protected abstract void modifyProperty(ARIProp column);

    /**
     * deletes an entire class from the remote server
     * @param table
     * @return
     */
    public abstract boolean dropClass(ARIClazz table);

    //auto|none|create|update
    public final boolean createClassIfNotExists(ARIClazz table){
        if (!classExists(table)){
            createClass(table);
        }
        createProperties(table.getProperties());
        return true;
    }

    
    public final void checkIntegrity(ARIClazz table){
        log.trace("Check integrity for class " + table);
        if (!classExists(table)){
            log.trace("Class " + table + " does not exists");
            createClass(table);
            createProperties(table.getProperties());
        } else {
            log.trace("Class " + table + " already exists, perform integrity check:");
            checkPropertiesIntegrity(table.getProperties());
        }
    }

    private void checkPropertiesIntegrity(List<ARIProp> properties){
        for (ARIProp prop: properties){
            if (!propertyExists(prop)){
                log.trace("Property " + prop + " not found, create it!!!");
                createProperty(prop);
            } else {
                if(clazzHasNoProps(prop.getTable())){
                    log.trace("Table is empty, perform drop and recreate for " + prop);
                    dropProperty(prop);
                    createProperty(prop);
                }
                else {
                    if (!propertyMatchExistingSchema(prop)){
                        log.trace("Table is not empty, perform modify " + prop);
                        modifyProperty(prop);
                    }  else {
                        log.trace(prop + " match existing definition");
                    }
                }
            }
        }
    }

    /**
     * check if provided property match the schema from the remote server
     * @param prop
     * @return
     */
    protected abstract boolean propertyMatchExistingSchema(ARIProp prop);

    /**
     * should return false if defined class has no properties defined inside the remote server
     * @param table
     * @return
     */
    public abstract boolean clazzHasNoProps(ARIClazz table);




    private void createProperties(Iterable<ARIProp> props){
        for (ARIProp c: props) {
            if (!propertyExists(c)){
                createProperty(c);
            }
        }
    }

    public abstract void put(ARIClazz table, List<ARIValue> values, Event<Integer> event);


    public final void put(ARIClazz table, Event<Integer> event,  ARIValue... props){
        List<ARIValue> vals = Arrays.asList(props);
        put(table, vals, event);
    }

    public final void put(ARIClazz table,  ARIValue... props){
        put(table, DEFAULT_EVENT, props);
    }

    public abstract void get(ARIClazz table, List<ARIProp> propsToFetch, Event<R> event,  List<ARIValue> clauses);

    /**
     *
     * @param table the class to modify
     * @param propertiesToChange the properties with new values to be changed
     * @param event
     * @param clauses the
     */
    public void update(ARIClazz table, List<ARIValue> propertiesToChange, Event<Integer> event, List<ARIValue> clauses){

    }

    /**
     *
     * @param table
     * @param propsToFetch - properties to be fetched
     * @param event
     * @param clauses
     */
    public final void get(ARIClazz table, ARIProp[] propsToFetch, Event<R> event,  ARIValue... clauses){
        List<ARIValue> vals = Arrays.asList(clauses);
        List<ARIProp> props;
        if (propsToFetch != null){
            props = Arrays.asList(propsToFetch);
        } else {
            props = Collections.emptyList();
        }

        get(table, props, event, vals);
    }

    public final void get(ARIClazz table, Event<R> event,  ARIValue... clauses){
        List<ARIValue> vals = Arrays.asList(clauses);
        get(table, Collections.<ARIProp>emptyList(), event, vals);
    }




    public interface Event<T> {
        void taskComplete(T args);
    }

    public static final Event<Integer> DEFAULT_EVENT = new Event<Integer>() {
        @Override
        public void taskComplete(Integer args) {
            System.out.println("SUCCESS:" + args);
        }
    };
}
