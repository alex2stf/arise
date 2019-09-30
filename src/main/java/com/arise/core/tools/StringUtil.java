package com.arise.core.tools;



import java.io.*;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

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


    public static Map<String, List<String>> getQueryParams(String uri){


        Map<String, List<String>> r = new HashMap<String, List<String>>();
        if (uri == null || uri.trim().isEmpty() || uri.trim().equalsIgnoreCase("/")){
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
        String r = "";
        r+=list[0];
        for (int i = 1; i < list.length; i++){
            r+="," + list[i];
        }
        return r;
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
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (T value: values){
            if (cnt > 0){
                sb.append(delimiter);
            }
            sb.append(iterator.toString(value));
            cnt++;
        }
        return sb.toString();
    }

    public static <T> String join(T[] values, String delimiter, JoinIterator<T> iterator) {
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

    public static String joinFormat(Object ... args){
        return joinArgs(" ", args);
    }

    public static String joinArgs(String delimiter, Object ... args)
    {
        boolean first = true;
        StringBuilder out = new StringBuilder();
        for (Object s: args){
            if (first)
            {
                first = false;
            }
            else
            {
                out.append(delimiter);
            }
            out.append(String.valueOf(s));
        }

        return out.toString();
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
            return String.valueOf(c.getLocalAddress() + ":" + c.getLocalPort()
                    + "|" + c.getRemoteSocketAddress());
        }
        return String.valueOf(o);
    }

    public static Set<String> smallestString(Set<String> in) {
        if (in.size() == 1){
            return in;
        }

        Set<String> rs = new HashSet<String>();
        String r = null;
        int max = 0;
        for (String s: in) {
            if (s.length() > max){
                max = s.length();
            }
        }

        for (String s: in) {
            if (s.length() < max){
                max = s.length();
                r = s;
            }
        }

        rs.add(r);
        return rs;
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
            return String.valueOf(value);
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

    public interface JoinIterator<T> {
        String toString(T value);
    }

    public static String putBetweenQuotes(String s){
        if (!s.startsWith("\"")){
            s = "\"" + s;
        }
        if (!s.endsWith("\"")){
            s = s + "\"";
        }

        return s;
    }
}
