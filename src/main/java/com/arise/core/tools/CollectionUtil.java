package com.arise.core.tools;

import com.arise.core.exceptions.LogicalException;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoProvider;
import com.sun.org.apache.xerces.internal.xs.StringList;
import org.omg.CORBA.MARSHAL;

import java.util.*;

import static com.arise.core.tools.AppCache.storeList;
import static com.arise.core.tools.StringUtil.join;
import static com.arise.core.tools.Util.randBetween;
import static java.util.Collections.shuffle;


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
        return TypeUtil.isNull(map) || map.isEmpty();
    }



    public static <T> boolean isEmpty(List<T> list) {
        return TypeUtil.isNull(list) || list.isEmpty();
    }

    public static <T> boolean isEmpty(Collection<T> c) {
        return TypeUtil.isNull(c) || c.isEmpty();
    }

    public static <T> boolean isEmpty(Set<T> set) {
        return TypeUtil.isNull(set) || set.isEmpty();
    }

    public static boolean mapContains(Object obj, String name) {
       return !TypeUtil.isNull(obj) && (obj instanceof Map) && ((Map)obj).containsKey(name);
    }

    public static <T> boolean isEmpty(T[] any) {
        return TypeUtil.isNull(any) || any.length == 0;
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

    public static <T> List<T> concat(List<T> a, List<T> b) {
        for (T i: b){
            a.add(i);
        }
        return a;
    }

    public static <T> T safeGetItem(List<T> args, int i, T def) {
        if (i <= args.size() -1){
            return args.get(i);
        }
        return def;
    }



    public static <T> T pickOne(List<T> s) {
        return s.get(randBetween(0, s.size() ));
    }

    public static <T> List<T> removeFirst(int s, List<T> x){
        return sublist(s, x.size(), x);
    }

    public static <T> List<T> sublist(int s, int e, List<T> x){
        if(isEmpty(x) || x.size() == 1){
            return x;
        }
        List<T> cp = new ArrayList<>();
        if (s < 0 ){
            throw new LogicalException("List size(" + x.size() + ") not in range (" + s + "," + e + ")" );
        }
        if (e > x.size()){
            e = x.size();
        }
        for(int i = s; i < e; i++){
            cp.add(x.get(i));
        }
        return cp;
    }

    public static <T> T randomPickElement(Collection<T> c){
        int size = c.size();
        if(isEmpty(c)){
            return null;
        }
        if(size == 1){
            return c.iterator().next();
        }
        int rand = (int) Math.round((Math.random() * size) + 0);
        int i = 0;
        for (T t: c){
            if (i == rand){
                return t;
            }
            i++;
        }
        return c.iterator().next();
    }

    public static String randomPickFromPersistentList(List<String> s, String name) {
       return pickFromPersistentList(s, true, name);
    }

    public static String pickFromPersistentList(List<String> s, boolean dSh, String name) {
        if(s.size() == 1){
            return s.get(0);
        }
        String k = "L_" + (dSh ? "_shuffled" : "_linear") + "_" +  StringUtil.sanitizeAppId(name);
        AppCache.StoredList l = AppCache.getStoredList(k);
        if (l.isEmpty() || l.isIndexExceeded()){
            if(dSh){
                Mole.getInstance("CLCTC_UTI").info("shuffle " + k);
                shuffleList(s);
            }
            l = storeList(k, s, 0);
        }
        int i = l.getIndex();
        storeList(k, l.getItems(), l.getIndex() + 1);
        Mole.getInstance("CLCTC_UTI").info(  k + " return index = " + i);
        return l.getItems().get(i);
    }


    private static synchronized void shuffleList(List<String> s){

        Map<String, String> t = ContentInfoProvider.getTitles();
        Map<String, String> artisti = new HashMap<>();
        for (Map.Entry<String, String> e: t.entrySet()){
            artisti.put(e.getKey(), getArtist(e.getValue()));
        }

        Map<String, List<String>> buf = new HashMap<>();

        for (String p : s){
            String k = StringUtil.hasContent(artisti.get(p)) ? artisti.get(p) : "null";
            MapUtil.addItemToArrayList(buf, k, p);
        }

        Set<String> toremove = new HashSet<>();
        List<String> onepound = new ArrayList<>();
        for (Map.Entry<String, List<String>> e: buf.entrySet()){
            if(e.getValue().size() == 1){
                onepound.add(e.getValue().get(0));
                toremove.add(e.getKey());
            }
        }

        for (String k: toremove){
            buf.remove(k);
        }
        buf.put(UUID.randomUUID().toString(), onepound);


        List<String> shArts = new ArrayList<>(buf.keySet());
        Collections.shuffle(shArts);
        int maxSize = 1;

        for (String a: shArts){
            shuffle(buf.get(a));
            if(buf.get(a).size() > maxSize){
                maxSize = buf.get(a).size();
            }
        }


        List<String> res = new ArrayList<>();

        for (int i = 0; i < maxSize; i++){
            for (String artist: shArts) {
                List<String> pl = buf.get(artist);
                if(pl.size() > i){
                    res.add(pl.get(i));
                }
            }
        }


        //afla coliziunile:
        int coliziuni = 0;
        for (int i = 0; i <  res.size(); i++){
            String curr = res.get(i);
            String prev = "";
            if(i > 0){
                prev = res.get(i-1);
            }
            String currArt = artisti.get(curr) + "";
            String preArt = artisti.get(prev) + "";

            if(currArt.equals(preArt) && !"null".equals(currArt) && !"null".equals(preArt)){
                coliziuni++;
            }
        }

        System.out.println("NR DE COLIZIUNI IN RANDOM = " + coliziuni);



    }

    private static String getArtist(String s){
        return String.valueOf((s + "").split("-")[0].trim().toLowerCase());
    }















    public interface SmartHandler<T> {
        void  handle(T t1, T t2);
    }

    public interface CMatcher<T> {
        boolean match(Collection<T> collection, T object);
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

}
