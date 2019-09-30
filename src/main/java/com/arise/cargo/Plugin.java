package com.arise.cargo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class Plugin {

  private final String _id;
  private HashMap<String, Worker> workers = new HashMap<>();

  protected Plugin(String id) {
    this._id = id;
  }

  @Override
  public String toString() {
    return "Plugin{" +
        "_id='" + _id + '\'' +
        ", workers=" + workers.values().size() +
        '}';
  }

  public final String id(){
    return _id;
  }


  public Collection<Worker> getWorkers(){
    return workers.values();
  }



  @Override
  public int hashCode() {
    return _id.hashCode();
  }

  public final Worker getWorker(String name){
    if (!workers.containsKey(name)){
      workers.put(name, buildWorker(name));
    }
    return workers.get(name);
  }

  protected abstract Worker buildWorker(String name);

  public static abstract class Worker {

    protected final String name;
    protected final Plugin parent;

    private List<String[]> instructions = new ArrayList<>();
    private String[] namespace;

    public Worker(String name, Plugin parent){
      this.name = name;
      this.parent = parent;
    }

    public String[] getNamespace() {
      return namespace;
    }

    public Worker setNamespace(String[] currentNamespace){
      this.namespace = currentNamespace;
      return this;
    }

    public String getName() {
      return name;
    }

    public Plugin getParent() {
      return parent;
    }

    public List<String[]> getInstructions() {
      return instructions;
    }

    public final Worker addInstructions(String ... args){
      instructions.add(args);
      return this;
    }

    public abstract void execute(Context context, File output);


  }
}
