package com.arise.weland.impl;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.B64;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringEncoder;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.Convertor;
import com.arise.weland.dto.ContentInfo;

import javax.management.MalformedObjectNameException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.TypeUtil.isNull;
import static com.arise.core.tools.Util.close;

public class SuggestionService {
    Map root = new HashMap();
    private CacheStrategy cacheStrategy;
    private List<Convertor<Data, ContentInfo>> convertors;

    private static final Mole log = Mole.getInstance(SuggestionService.class);

    public SuggestionService addConvertor(Convertor<Data, ContentInfo> convertor){
        if (convertors == null){
            convertors = new ArrayList<>();
        }
        convertors.add(convertor);
        return this;
    }





    public SuggestionService load(String path){
        InputStream inputStream = FileUtil.findStream(path);
        if (inputStream == null){
            return this;
        }

        String cnt = StreamUtil.toString(inputStream).replaceAll("\r\n", " ");

        Map<Object, Object> local = (Map<Object, Object> ) Groot.decodeBytes(cnt);
        if (local == null){
            return this;
        }
        for (Map.Entry<Object, Object> e: local.entrySet()){
            root.put(String.valueOf(e.getKey()), e.getValue());
        }
        return this;
    }

    private Map getSuggestion(String  val){
        List suggestions = MapUtil.getList(root,"suggestions");
        if (suggestions == null){
            return null;
        }
        for (Object o: suggestions){
            if (o instanceof Map){
                Map m = (Map) o;
                if (m.containsKey("key")){
                    if (String.valueOf(m.get("key")).equalsIgnoreCase(val)){
                        return m;
                    }
                }
            }
        }
        return null;
    }


    public Data solveThumbnail(ContentInfo contentInfo){

        Data response = null;
        if (StringUtil.hasText( contentInfo.getThumbnailId() )){
            response = solveUrlOrBase64(contentInfo.getThumbnailId());
        }
        if (response == null){
            response = searchIcons(contentInfo.getPath());
        }


        if (response == null && !CollectionUtil.isEmpty(convertors)){
            for (Convertor<Data, ContentInfo> c: convertors){
                response = c.convert(contentInfo);
                if (response != null){
                    return response;
                }
            }
        }
        return response;
    }


    public Data searchIcons(String filename) {


        if (!StringUtil.hasContent(filename)){
            return null;
        }
        String parts[] = filename.split("\\\\");
        String next = parts[parts.length - 1];
        parts = next.split("/");
        next = parts[parts.length - 1];
        parts = next.split("\\.");
        next = parts[0];

        parts = next.split("\\s+");
        List<String> vals = new ArrayList<>();
        for (int i = 0; i < parts.length; i++){
            String k = parts[i].trim();
            if (isValidWord(k)){
                vals.add(k);
            }
        }

        List<String> combs =  getLinearCombinations(vals);
        String ext[] = filename.split("\\.");
        if (ext.length > 1){
            String last = ext[ext.length -1];
            combs.add("_" + last);
        }
        //search for icons:
        for (String s: combs){
            Map x = getSuggestion(s);
            if (x != null){
                List<String> icons = MapUtil.getList(x, "icons");
                if (!isEmpty(icons)){

                    for(String icon: icons){
                        Data d = solveUrlOrBase64(icon);
                        if (d != null){
                            return d;
                        }
                    }
                }

            }

        }

        return null;
    }








    public SuggestionService setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
        return this;
    }

    private static final String images_extension[] = new String[]{"png", "jpeg", "jpg"};

    public Data solveUrlOrBase64(String input){
        String id = StringEncoder.encodeShiftSHA(input + "", "xx");
        for (String ext: images_extension){
            String p = id + "." + ext;
            if (cacheStrategy.contains(p)){
                return cacheStrategy.get(p);
            }
        }


        if (input.startsWith("data:")){
            try {
                Tuple2<byte[], ContentType> res = decodeBase64Image(input);
                id = id + "." + res.second().mainExtension();
                Data data = new Data(id, res.first(), res.second());
                cacheStrategy.put(id, data);
                return data;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        else if (input.startsWith("http") && input.indexOf("/") > -1){
            URL uri;
            try {
                uri = new URL(input);
                String exts[] = uri.getPath().split("\\.");
                ContentType contentType = ContentType.search(exts[exts.length - 1]);
                if (contentType.equals(ContentType.TEXT_PLAIN)){
                    contentType = ContentType.IMAGE_JPEG;
                }
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    do_get(uri, byteArrayOutputStream);
                    id = id + "." + contentType.mainExtension();
                    Data data = new Data(id , byteArrayOutputStream.toByteArray(), contentType);
                    cacheStrategy.put(id, data);
                    return data;
                }
                catch (SuggestionFetchException e){
                    log.error("Failed to solve suggestion for " + uri);
                    return null;
                }
                catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    public void do_get(URL url, OutputStream out){
        HttpURLConnection connection = null;
        try {
            connection = DependencyManager.getConnection(url);
        } catch (IOException e) {
            close(connection);
            throw new SuggestionFetchException("Failed to obtain connection to " + url, e);
        }

        log.info("GET " + url);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        InputStream in = null;
        try {
            in = connection.getInputStream();
        } catch (IOException e) {
            close(in);
            close(connection);
            throw new SuggestionFetchException("Failed to obtain stream from " + url, e);
        }
        byte[] buf = new byte[8192];
        int length = 0;

        while (true){
            try {
                if (!((length = in.read(buf)) > 0)) break;
            } catch (Exception e) {
                close(in);
                close(connection);
                throw new SuggestionFetchException("Failed to read stream from " + url, e);
            }
            try {
                out.write(buf, 0, length);
                try {
                    out.flush();
                }catch (IOException e){
                    close(in);
                    close(connection);
                    close(out);
                    throw new SuggestionFetchException("Failed to flush file stream", e);
                }
            } catch (IOException e) {
                close(in);
                close(connection);
                close(out);
                throw new SuggestionFetchException("Failed to write stream ", e);
            }

        }
    }


    public static Tuple2<byte[], ContentType> decodeBase64Image(String input) throws Exception {
        int sepIndex = input.indexOf(",");
        String start = input.substring(0, sepIndex);
        String ctype = start.substring(start.indexOf(":") + 1, start.indexOf(";"));
        ContentType contentType = ContentType.search(ctype);
        String content = input.substring(sepIndex + 1);
        byte[] bytes =   B64.decodeToByteArray(content);;
        return new Tuple2<>(bytes, contentType);
    }




    char disallowed[] = new char[]{'~', '-'};
    private boolean isValidWord(String s){
        if (isNull(s)){
            return false;
        }
        if (s.length() == 1) {
            for (char c: disallowed){
                if (c == s.charAt(0)){
                    return false;
                }
            }
        }
        return true;
    }



    private List<String> getLinearCombinations(List<String> vals) {

        List<String> r = new ArrayList<>();
        if (vals.size() == 1){
            r.add(vals.get(0));
            r.add(vals.get(0).toLowerCase());
            return r;
        }
        for (int j = 0; j < vals.size(); j++){
            String c = vals.get(j);
            for (int i = j + 1; i < vals.size(); i++){
                r.add(c + " " + vals.get(i));
                c+=" " + vals.get(i);
            }
        }
        Collections.sort(r, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return Integer.compare(s.length(), t1.length());
            }
        });
        Collections.reverse(r);
        return r;
    }

    public abstract static class CacheStrategy {
        public abstract boolean contains(String id);
        public abstract Data get(String id);
        public abstract void put(String id, Data data);
    }

    public static class Data {
        public final String id;
        public final byte[] bytes;
        public final ContentType contentType;

        public Data(String id, byte[] bytes, ContentType contentType) {
            this.id = id;
            this.bytes = bytes;
            this.contentType = contentType;
        }


        public String getId() {
            return id;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public ContentType getContentType() {
            return contentType;
        }
    }


    public static class SuggestionFetchException extends RuntimeException {

        private final String m;
        private final Throwable c;

        public SuggestionFetchException(String m, Throwable c){
            super(m, c);
            this.m = m;
            this.c = c;
        }


    }

}
