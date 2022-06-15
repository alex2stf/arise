package com.arise.core.tools;


import com.arise.core.models.FilterCriteria;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.valueOf;


public class StringUtil {

    public static  int extractInt(String input , int dv){
        Integer x = extractInteger(input);
        if (x != null){
            return x;
        }
        return dv;
    }

    public static Integer extractInteger(String input){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++){
            char c = input.charAt(i);
            if (Character.isDigit(c)){
                sb.append(c);
            }
        }
        Integer i;
        try {
            i = Integer.valueOf(sb.toString());
        }catch (Exception e){
            i = null;
        }
        return i;
    }


    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         c = 0;
        int          i;
        int          len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String       t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }


    public static URLDecodeResult urlDecode(String in){
        in = in.trim();
        URLDecodeResult urlDecodeResult = new URLDecodeResult();
        if ("/".equals(in)){
            return urlDecodeResult;
        }
        int index = in.indexOf("?");
        String parts[] = (index > -1 ? in.substring(0, index) : in).split("/");

        for (String s: parts){
            if (StringUtil.hasContent(s)){
                urlDecodeResult.paths.add(s);
            }
        }

        String qrs = in.substring(index + 1 );
        decodeQuery(qrs, urlDecodeResult.queryParams);
        return urlDecodeResult;
    }


    public static Map<String, List<String>> decodeQueryParams(String in){
        Map<String, List<String>> buf = new HashMap<>();
        decodeQuery(in, buf);
        return buf;
    }

    public static void decodeQuery(String in, Map<String, List<String>> buffer){
        if (in == null){
            return;
        }
        int eIndex = in.indexOf("=");
        if (eIndex < 0){
            return;
        }
        String key = in.substring(0, eIndex);
        buffer.put(key, new ArrayList<String>());

        String rest = in.substring(eIndex + 1);
        int sIndex = rest.indexOf("&");
        if (sIndex < 0){
            CollectionUtil.concat(buffer.get(key), Arrays.asList(rest.split(",")));
            return;
        } else {
            //1=2&3=4
            String value = rest.substring(0, sIndex);
            buffer.get(key).add(value);
            decodeQuery(rest.substring(sIndex + 1), buffer);
        }
    }

    public static boolean endsWithNewline(String s) {
        return s.endsWith("\n") || s.endsWith("\r\n");
    }




    public static class URLDecodeResult {
        List<String> paths = new ArrayList<>();
        Map<String, List<String>> queryParams = new LinkedHashMap<>();

        public List<String> getPaths() {
            return paths;
        }

        public Map<String, List<String>> getQueryParams() {
            return queryParams;
        }
    }



    public static Map<String, List<String>> getQueryParams(String uri){


        Map<String, List<String>> r = new HashMap<String, List<String>>();
        if (!hasText(uri) || "/".equalsIgnoreCase(uri.trim())){
            return r;
        }
        String p[] = uri.split("\\&");
        for (String s: p){
            if (s != null && !s.trim().isEmpty()){
                String q[] = s.split("=");
                String key = null;
                List<String> value = new ArrayList<String>();
                if (q.length > 0){
                    key = q[0];
                    if (q.length > 1){
                        String qs[] = q[1].split(",");
                        value = Arrays.asList(qs);
                    }
                }

                if(key != null){
                    r.put(key, value);
                }


            }
        }
        return r;
    }


    public static String toCSV(String list[]){
        return join(list, ",");
    }

    public static String toCSV(Collection<String> strings){
        int c = 0;
        String r = "";
        for (String s: strings){
            if (c > 0){
                r+=',' + s;
            } else {
                r+=s;
            }
            c++;
        }
        return r;
    }

    public static String join(String item, int count, String delimiter){
        StringBuilder sb = new StringBuilder();
        sb.append(item);
        for (int i = 1; i < count; i++){
            sb.append(delimiter).append(item);
        }
        return sb.toString();
    }

    public static String join(int length, String repeatable, String delimiter){
        if (length == 0){
            return "";
        }
        if (length == 1){
            return repeatable;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++){
            if (i > 0){
                sb.append(delimiter);
            }
            sb.append(repeatable);
        }
        return sb.toString();
    }

    public static <T> String join(Iterable<T> values, String delimiter, JoinIterator<T> iterator) {
        if (values == null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int cnt = 0;

        if (values instanceof List){
            List<T> cp = (List) values;
            for (cnt = 0; cnt < cp.size(); cnt++){
                if (cnt > 0){
                    sb.append(delimiter);
                }
                sb.append(iterator.toString(cp.get(cnt)));
            }

        } else {
            for (T value : values) {
                if (cnt > 0) {
                    sb.append(delimiter);
                }
                sb.append(iterator.toString(value));
                cnt++;
            }
        }
        return sb.toString();
    }

    public static <T> String join(T[] values, String delimiter, JoinIterator<T> iterator) {
        if(null == values){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++){
            if (i > 0){
                sb.append(delimiter);
            }
            sb.append(iterator.toString(values[i]));
        }
        return sb.toString();
    }

    public static <T> String join(T[] values, String delimiter) {
        return join(values, delimiter, DEFAULT_ITERATOR);
    }

    public static <T> String join(Iterable<T> values, String delimiter) {
        return join(values, delimiter, DEFAULT_ITERATOR);
    }

    public static String urlDecodeUTF8(String input){
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //TODO throw runtime exception
            e.printStackTrace();
        }
        return input;
    }

    public static String urlEncodeUTF8(String input){
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            //TODO throw runtime exception
            e.printStackTrace();
        }
        return input;
    }

    public static Integer toInt(String s){
        if (s == null){
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException ex){
            return null;
        }
    }


    public static int toInt(String s, int d){
        Integer x = toInt(s);
        if (null == x){
            return d;
        }
        return x;
    }









    public static boolean isWildcardMatch(String s, String p) {
        int i=0, j=0;
        int ii=-1, jj=-1;

        while(i<s.length()) {
            if(j<p.length() && p.charAt(j)=='*') {
                ii=i;
                jj=j;
                j++;
            } else if(j<p.length() &&
                    (s.charAt(i) == p.charAt(j) ||
                            p.charAt(j) == '?')) {
                i++;
                j++;
            } else {
                if(jj==-1) return false;

                j=jj;
                i=ii+1;
            }
        }

        while(j<p.length() && p.charAt(j)=='*') j++;

        return j==p.length();
    }




    public static String dump(Object o) {
        if (o instanceof Throwable){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ((Throwable) o).printStackTrace(pw);
            return sw.toString(); // stack trace as a string
        }

        if (o instanceof SocketChannel){
            SocketChannel c = (SocketChannel) o;
            if (c.socket() != null){
                return dump(c.socket());
            }
        }



        if (o instanceof Socket){
            Socket c = (Socket) o;
            return valueOf(c.getLocalAddress() + ":" + c.getLocalPort()
                    + "|" + c.getRemoteSocketAddress());
        }


        return valueOf(o);
    }

    public static String sanitizeAppId(String s){
        if ("LAYNEE_WALL".equals(s)){
            return s;
        }
        return  ("" + s).replaceAll("\\s+","")
                .replaceAll("http:", "L")
                .replaceAll("https:", "U")
                .replaceAll(":", "Q")
                .replaceAll("\\(", "_")
                .replaceAll("\\)", "_")
                .replaceAll("=", "v")
                .replaceAll("\\?", "z")
                .replaceAll("\\.", "d")
                .replaceAll("\\+", "5")
                .replaceAll("/", "")
                .replaceAll("inux", "Xx")
                .replaceAll("samsung", "sG")
                .replaceAll("aarch", "yH")
                .replaceAll("\"", "")
                .replaceAll("\\\\", "")
                .replaceAll("%2", "s")
                .replaceAll("%28", "7")
                .replaceAll("#", "_")
                .replaceAll("%", "x")
                .replaceAll("//", "g")
                .replaceAll("storage", "y")
                .replaceAll("ovies", "W")
                .replaceAll("usic", "R")
                .replaceAll("/", "7")
                .replaceAll("mate", "M3")
                .replaceAll("generic", "89")
                .replaceAll("alex", "SAP");
    }


    /**
     * Read a line from the given stream.
     */
    public static String readLine(InputStream in, String charset) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true) {
            // Read one byte from the stream.
            int b = in.read();

            // If the end of the stream was reached.
            if (b == -1)
            {
                if (baos.size() == 0)
                {
                    // No more line.
                    return null;
                }
                else
                {
                    // The end of the line was reached.
                    break;
                }
            }

            if (b == '\n')
            {
                // The end of the line was reached.
                break;
            }

            if (b != '\r')
            {
                // Normal character.
                baos.write(b);
                continue;
            }

            // Read one more byte.
            int b2 = in.read();

            // If the end of the stream was reached.
            if (b2 == -1)
            {
                // Treat the '\r' as a normal character.
                baos.write(b);

                // The end of the line was reached.
                break;
            }

            // If '\n' follows the '\r'.
            if (b2 == '\n')
            {
                // The end of the line was reached.
                break;
            }

            // Treat the '\r' as a normal character.
            baos.write(b);

            // Append the byte which follows the '\r'.
            baos.write(b2);
        }

        // Convert the byte array to a string.
        return baos.toString(charset);
    }



    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isMailFormat(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }


    public static Number toNumber(String s){
        return toNumber(s, '.');
    }

    public static Number toNumber(String s, char q) {
        if (!StringUtil.hasContent(s)){
            return null;
        }
        boolean neg = false;
        int type = 0;

        if (s.charAt(0) == '-'){
            if (s.length() == 1){
                return null;
            }
            neg = true;
            s = s.substring(1);
        }

        char c;
        int qc = 0;

        for(int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == q){ //check for quote
                qc++;
                if (qc > 1){
                    return null;
                }
                type = 1;
            }
            else if (!Character.isDigit(c)){
                return null;
            }
        }

        switch (type){
            case 0:
                Integer x = convert(s, Integer.class);
                if (neg){
                    x *= -1;
                }
                return x;
            case 1:
                Double z = convert(s, Double.class);
                if (neg){
                    z *= -1;
                }
                return z;
        }

        return null;
    }

    private static <T> T convert(String  s, Class<T> clazz){
       return (T) ReflectUtil.getStaticMethod(clazz, "valueOf", String.class).call(s);
    }

    public static boolean hasContent(String in) {
        return in != null && !(in.trim().isEmpty());
    }

    public static String removeLastChar(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static final JoinIterator DEFAULT_ITERATOR = new JoinIterator() {
        @Override
        public String toString(Object value) {
            return valueOf(value);
        }
    };

    public static final JoinIterator QUOTE_ITERATOR = new JoinIterator() {
        @Override
        public String toString(Object value) {
            return jsonQuote(valueOf(value));
        }
    };

    public static String capFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String lowFirst(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    private static final String[] chars = "1234567890qwertyuioppasdfghjklzxcvbnmmQWERTYUIOPLKJHGFDSAZXCVBNM".split("");

    public static String randStr(){
        int rand = (int) Math.round(Math.random() * (chars.length -1) );
        String x = chars[rand];
        return UUID.randomUUID().toString().replaceAll("-", x);
    }

    public static String toSnakeCase(String in){
        if (in == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i++){
            char c = in.charAt(i);
            if (Character.isLowerCase(c)){
                sb.append(c);
            }
            else {
                if (i > 0 && c != '_'){
                    sb.append("_");
                }
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    public static boolean bytesEndWithString(byte bytes[], String in){
        if (in.length() > bytes.length){
            return false;
        }
        for (int i = in.length() - 1, j = bytes.length - 1;
             i > -1; i--, j--){
            if (in.charAt(i) != bytes[j]){
                return false;
            }
        }
        return true;
    }

    public static String[] splitByFirstOccurrence(String input, String sub){
        int index = input.indexOf(sub);
        return new String[]{input.substring(0, index), input.substring(index + sub.length())};
    }

    public static String[] splitByWhitespace(String s) {
        return s.split("\\s+");
    }

    public static <T> String join(T[] p, int from, int to, String separator, JoinIterator<T> iterator) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++){
            if (i > from){
                sb.append(separator);
            }
            sb.append(iterator.toString(p[i]));
        }
        return sb.toString();
    }

    public static boolean hasText(String in) {
        return hasContent(in);
    }

    public static String trim(String input) {
        return (input != null ? input.trim() : input);
    }

    public static <A, B> String merge(A[]a, B[]b, String d1, String d2, JoinIterator<A> aj, JoinIterator<B> bj) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++){
            if (i > 0){
                sb.append(d2);
            }
            sb.append(aj.toString(a[i]));
            sb.append(d1);
            sb.append(bj.toString(b[i]));

        }
        return sb.toString();
    }



    public interface JoinIterator<T>  {
        String toString(T value);
    }




    public static String map(String value, Map<String, Object> map){
        return map(value, map, TypeUtil.defaultFieldFiltering, TypeUtil.getterMethodFiltering);
    }


    /**
     * @param value
     * @param map
     * @return
     */
    public static String map(String value, Map<String, Object> map, FilterCriteria<Field> ff, FilterCriteria<Method> mf){
        if (!hasText(value) || value.length() < 3 || CollectionUtil.isEmpty(map)){
            return value;
        }
        Matcher m = Pattern.compile("(?<!\\\\)\\{(.*?)\\}").matcher(value);
        while (m.find()){
            String s = m.group();
            String key = s.substring(1, s.length() - 1);
            Object vr = null;
            if (key.indexOf('.') > -1){
                vr = TypeUtil.search(key.split("\\."), map, 0, ff, mf);
            } else {
                vr = map.get(key);
            }
            value = value.replaceAll(Pattern.quote(s), valueOf(vr));
        }
        return value;
    }


    public static String jsonEscape(String s){
        StringBuffer sb = new StringBuffer();
        StringUtil.jsonEscape(valueOf(s), sb);
        return sb.toString();
    }

    public static String jsonQuote(String s){
        return "\"" + jsonEscape(s) + "\"";
    }

    public static String  jsonVal(Object s){
       if (s == null){
           return "null";
       }
       if (s instanceof CharSequence){
           return "\"" + jsonEscape(valueOf(s)) + "\"";
       }

       return s.toString();
    }


    @Deprecated
    public static String jsonVal(Collection s){
        StringBuilder sb = new StringBuilder();
        sb.append("[")
        .append(join(s, ",", QUOTE_ITERATOR))
        .append("]");
        return sb.toString();
    }





    public static void jsonEscape(String s, StringBuffer sb) {
        final int len = s.length();
        for(int i=0;i<len;i++){
            char ch=s.charAt(i);
            switch(ch){
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                    if((ch>='\u0000' && ch<='\u001F') || (ch>='\u007F' && ch<='\u009F') || (ch>='\u2000' && ch<='\u20FF')){
                        String ss=Integer.toHexString(ch);
                        sb.append("\\u");
                        for(int k=0;k<4-ss.length();k++){
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    }
                    else{
                        sb.append(ch);
                    }
            }
        }//for
    }


    public static String anyToString(Object o,
                                     JoinIterator keyIterator,
                                     JoinIterator valueIterator,
                                     String mapDelimiter,
                                     String listDelimiter,
                                     String listBegin,
                                     String listEnd){
        if (o instanceof Iterable) {
            return listBegin + join((Iterable) o, listDelimiter, valueIterator) + listEnd;
        } else if (o instanceof Map) {
            return join((Map)o, keyIterator, valueIterator, mapDelimiter, listDelimiter, listBegin, listEnd);
        }
        return valueIterator.toString(o);
    }

    public static <K, V> String join(Map<K, V> map,
                                     JoinIterator<K> keyIterator,
                                     JoinIterator<V> valueIterator,
                                     String mapDelimiter,
                                     String listDelimiter,
                                     String listBegin,
                                     String listEnd){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<K, V> entry: map.entrySet()) {
            String right = anyToString(entry.getValue(), keyIterator, valueIterator, mapDelimiter, listDelimiter, listBegin, listEnd);
            String left = anyToString(entry.getKey(), keyIterator, valueIterator, mapDelimiter, listDelimiter, listBegin, listEnd);

            sb.append(left)
                    .append(mapDelimiter)
                    .append(right);

        }
        return sb.toString();

    }



    public static JsonBuilder jsonBuilder(){
        return new JsonBuilder();
    }


    public static class JsonBuilder {
        private StringBuilder sb = new StringBuilder();
        public JsonBuilder objectStart(){
            buffer = new ArrayList<>();
            sb.append("{");
            return this;
        }
        public JsonBuilder objectEnd(){
            sb.append(join(buffer, ","));
            sb.append("}");
            return this;
        }
        List<String> buffer = new ArrayList<>();

        public JsonBuilder add(String key, String value){
            buffer.add(jsonQuote(key) + ":" + jsonQuote(value));
            return this;
        }

        public <T> JsonBuilder add(String key, Collection value){
            return add(key, value, DEFAULT_ITERATOR);
        }

        public <T> JsonBuilder add(String key, Collection value, JoinIterator joinIterator){
            buffer.add(jsonQuote(key) + ": [" + join(value, ",", joinIterator) + "]");
            return this;
        }

        public JsonBuilder add(String key, String[] value){
            return add(key, value, DEFAULT_ITERATOR);
        }

        public JsonBuilder add(String key, String[] value, JoinIterator joinIterator){
            buffer.add(jsonQuote(key) + ": [" + join(value, ",", joinIterator) + "]");
            return this;
        }

        public <T,Z> JsonBuilder add(String key, Map<T, Z> value, JoinIterator<Z> iterator){
            if (null == value){
                return this;
            }
            StringBuilder sb = new StringBuilder();
            int cnt = 0;
            for(Map.Entry<T, Z> entry: value.entrySet()) {
                Z mpval = entry.getValue();
                String right;
                if (mpval instanceof Iterable) {
                    right = "[" + join((Iterable<Z>) mpval, ",", iterator) + "]";
                } else if (mpval instanceof Map) {
                    right = "\"map-to-do\"";
                } else {
                    right = iterator.toString(mpval);
                }
                if (cnt > 0){
                    sb.append(",");
                }
                sb.append(jsonQuote(entry.getKey() + "")).append(":")
                        .append(right);
                ;
//                sb.append("{}");

                cnt++;
            }
            buffer.add(jsonQuote(key) + ": {" + sb + "}");
            return this;
        }

        public String build(){
            return sb.toString();
        }


    }
}
