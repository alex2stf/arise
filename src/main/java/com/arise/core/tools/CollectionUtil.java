package com.arise.core.tools;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arise.core.tools.TypeUtil.isNull;

public class CollectionUtil {
    public static boolean listContainsIgnorecase(String search, String[] items){
        for (String s: items){
            if (s.equalsIgnoreCase(search)){
                return true;
            }
        }
        return false;
    }







    public static  String[] toArray(List<String> s) {
        String[]c = new String[s.size()];
        return s.toArray(c);
    }


    public static <T> boolean hasData(T[] dimpl) {
        return dimpl != null && dimpl.length > 0;
    }

    public static <C, T> void merge(Map<C, T> source, Map<C, T> destination) {
        for(Map.Entry<C, T> e: source.entrySet()){
            destination.put(e.getKey(), e.getValue());
        }
    }

    public static <T, X> boolean isEmpty(Map<T, X> map) {
        return isNull(map) || map.isEmpty();
    }



    public static <T> boolean isEmpty(List<T> list) {
        return isNull(list) || list.isEmpty();
    }

    public static <T> boolean isEmpty(Collection<T> c) {
        return isNull(c) || c.isEmpty();
    }

    public static <T> boolean isEmpty(Set<T> set) {
        return isNull(set) || set.isEmpty();
    }

    public static boolean mapContains(Object obj, String name) {
       return !isNull(obj) && (obj instanceof Map) && ((Map)obj).containsKey(name);
    }

    public static <T> boolean isEmpty(T[] any) {
        return isNull(any) || any.length == 0;
    }


    public static <T> void scan(Collection<T> nvl, Collection<T> old, Handler<T> handler){
        scan(nvl, old, DEFAULT_MATCHER, handler);
    }

    public static <T> void scan(Collection<T> nvl, Collection<T> old,  CMatcher<T> m, Handler<T> handler ){
        if (isEmpty(nvl)){
            return;
        }
        if (isEmpty(old)){
            for (T t: nvl){
               handler.added(t);
            }
            return;
        }

        for (T t: old){
            if (m.match(nvl, t)){
                handler.same(t);
            } else {
               handler.removed(t);
            }
        }
        for (T t: nvl){
            if (!m.match(old, t)){
                handler.added(t);
            }
        }
    }

    public static <T> void smartIterate(List<T> data, SmartHandler<T> smartHandler) {
//        if (data.size() == 1){
//            smartHandler.handle(data.get(0), null);
//            return;
//        }
        int index = 0;
        int size = data.size();
        if (size % 2 != 0){
            size = size - 1;
        }

        index = (size / 2);
        for (int i = 0; i < index; i++){
            T t1 = data.get(i);
            T t2 = data.get(size - 1 - i);
            smartHandler.handle(t1, t2);
        }
        if (size < data.size()) {
            smartHandler.handle(data.get(size), null);
        }


    }



    public interface SmartHandler<T> {
        void  handle(T t1, T t2);
    }

    public interface CMatcher<T> {
        boolean match(Collection <T> collection, T object);
    }

    public static final CMatcher DEFAULT_MATCHER = new CMatcher() {
        @Override
        public boolean match(Collection c, Object o) {
            return c.contains(o);
        }
    };

    public interface Handler<T> {
        void added(T t);
        void removed(T t);
        void same(T t);
    }

    public static abstract class DiffHandler<T> implements  Handler<T> {
        @Override
        public void same(T t) {
            ;;
        }
    }
}
