package com.arise.core.tools;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alex2 on 12/06/2024.
 */
public class AppDispatcher {
    public static Map<String, Set<Event>> events = new ConcurrentHashMap<>();


    public static synchronized void on(String name, Event event){
        if(!events.containsKey(name)){
            events.put(name, new HashSet<Event>());
        }
        Set<Event> arr = events.get(name);
        arr.add(event);
        events.put(name, arr);
    }

    public static synchronized void dispatch(String name){
       if(events.containsKey(name)){
           for (Event e: events.get(name)) {
               try {
                   e.execute();
               } catch (Exception ex){
                   Mole.getInstance("APP_EVENT").warn("Error executing event" + name, ex);
               }
           }
       } else if(!"tick".equals(name)) {
           Mole.getInstance("APP_EVENT").warn("not found " + name);
       }
    }

    public static synchronized void tick(){
        dispatch("tick");
    }

    public static synchronized void onTick(Event event){
        on("tick", event);
    }



    public interface Event {
        void execute();
    }
}
