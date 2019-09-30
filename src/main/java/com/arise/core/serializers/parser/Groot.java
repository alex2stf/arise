package com.arise.core.serializers.parser;

import com.arise.core.models.DeviceStat;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.TypeUtil;
import com.arise.core.tools.TypeUtil.IteratorHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.arise.core.tools.TypeUtil.*;

public class Groot {

  public static Object decodeBytes(byte[] in, int firstIndex, int offset, Syntax s){
    //TODO improve this

    debug("IN", in, firstIndex, offset);

    while (s.isWhitespace(in[firstIndex])){
      firstIndex++;
    }

    while (s.isWhitespace(in[offset - 1])){
      offset--;
    }

    if (isMatch(in, firstIndex, offset, s.vtrue())){
      return true;
    }


    if (isMatch(in, firstIndex, offset, s.vnull())){
      return null;
    }



    if (isMatch(in, firstIndex, offset, s.vfalse())){
      return false;
    }


    if (isNumericSequence(in, firstIndex, offset, s.nd(), s.ng())){
      return toNumber(in, firstIndex, offset, s.nd(), s.ng());
    }



    int lastIndex = offset -1;
    int prevFrom = (firstIndex > 0) ? firstIndex -1 : firstIndex;
    int prevLast = (lastIndex > 0) ? lastIndex -1 : lastIndex;

    if ((char)in[firstIndex] == s.o1() && prevFrom != s.es() && prevLast != s.es() && (char)in[lastIndex] == s.o2()){
      return decodeObject(in, firstIndex + 1, lastIndex, s);
    }

    if (in[firstIndex] == s.a1() && prevFrom != s.es() && prevLast != s.es() && in[lastIndex] == s.a2()){
      return decodeArray(in, firstIndex + 1, lastIndex, s);
    }

    for (char c: s.quotes()){
      if (in[firstIndex] == c && in[lastIndex] == c){
        return decodeString(in, firstIndex + 1, lastIndex);
      }
    }

    debug("FAILED FOR", in, firstIndex, lastIndex);



    return null;
  }



  public static String toJson(Object obj) {
   return  toJson(obj, Syntax.STANDARD);
  }

  public static String toJson(Object obj, Syntax s) {
    if (isNull(obj)){
      return "null";
    }
    if (isNumber(obj)){
      return obj.toString();
    }

    if (isBoolean(obj)){
      return isBooleanTrue(obj) ? s.vtrue() : s.vfalse();
    }

    //TODO improve this
    if (obj instanceof String){
      return "\"" + escapeToString(obj) + "\"";
    }

    boolean isArray = TypeUtil.isSingleKeyIterable(obj);

    StringBuilder sb = new StringBuilder();
    char end;
    if (isArray){
      sb.append(s.a1());
      end = s.a2();
    } else {
      sb.append(s.o1());
      end = s.o2();
    }


    TypeUtil.forEach(obj, new IteratorHandler() {
      @Override
      public void found(Object key, Object value, int index) {

        if (isArray){
          sb.append(toJson(value, s));
        } else {
          sb.append("\"")
              .append(escapeToString(key))
              .append("\":")
              .append(toJson(value, s));
        }
        sb.append(",");
      }
    }, false, getJsonIgnoreAnnotationsAnnotations());


    sb.setCharAt(sb.length() - 1, end);
    return sb.toString();
  }



  private static String escapeToString(Object s){
    return String.valueOf(s);
  }


  static class BufHelper {

    final Syntax s;
    final int qts[];
    int objs = 0;
    int arrs = 0;

    public BufHelper(Syntax s) {
      this.s = s;
      qts = new int[s.quotes().length];
    }

    void track(char c) {
      for(int i = 0; i < s.quotes().length; i++){
        if (c == s.quotes()[i]){
          qts[i]++;
        }
      }
      if (c == s.o1()) objs++;
      else if (c == s.o2()) objs --;
      else  if (c == s.a1()) arrs ++;
      else if (c == s.a2()) arrs --;
    }


    boolean isSafe(){
      for (int s: qts){
        if(! (s%2==0)){
          return false;
        }
      }
      return objs == 0 && arrs == 0;
    }

  }


  private static boolean isMatch(byte[] in, int from, int to, String match){
    int x = 0;
    for (int i = from; i < to; i++){
      char c = (char) in[i];
      if (match.charAt(x) != c){
        return false;
      }
      x++;
    }
    return true;
  }

  private static String decodeString(byte[] in, int from, int to){
    StringBuilder sb = new StringBuilder();
    for (int i = from; i < to; i++){
      sb.append((char)in[i]);
    }
    return sb.toString();
  }

  private static Object decodeArray(byte[] in, int from, int to, Syntax s) {

    Arr response = new Arr();

    BufHelper helper = new BufHelper(s);
    int cursor = from;

    for (int i = from; i < to; i++){
      char c = (char) in[i];
      helper.track(c);

      if (helper.isSafe() && c == s.vd()){
        Object object = decodeBytes(in, cursor, i , s);
        response.add(object);
        cursor = i + 1;
      }

    }

    if (to > cursor){
      Object object = decodeBytes(in, cursor, to , s);
      response.add(object);
    }


    return response;
  }


  private static Object decodeObject(byte[] in, int from, int to, Syntax s) {
    Obj response = new Obj();


    int cursor = from;
    BufHelper helper = new BufHelper(s);

    String key = null;

    for (int i = from; i < to; i++){
      char c = (char) in[i];
      helper.track(c);

      if (helper.isSafe()){
          if (c == s.kd()){
            key = createKey(in, cursor, i, s);
            cursor = i + 1;
          }
          else if (c == s.vd()){
            Object obj = decodeBytes(in, cursor, i, s);
            response.put(key, obj);
            cursor = i + 1;

            //handle key + value generation
          }
      }
    }


    if (key != null) {
      Object obj = decodeBytes(in, cursor, to, s);
      response.put(key, obj);
    }
    return response;
  }


  private static void debug(String prefix, byte[] bytes, int from, int to){
//    StringBuilder full = new StringBuilder();
    StringBuilder sb = new StringBuilder();
    for (int i = from; i < to; i++){
      sb.append((char)bytes[i]);
    }
//    System.out.println(" FULL " + new String(bytes));
    System.out.println(prefix + "  (" + from + " to " + to + ")=" + sb.toString());
  }




  /**
   * TODO improve this
   * @param in
   * @param from
   * @param to
   * @param s
   * @return
   */
  private static String createKey(byte[] in, int from, int to, Syntax s){
    while (s.isWhitespace(in[from])){
      from++;
    }

    while (s.isWhitespace(in[to])){
      to--;
    }
    String key = decodeString(in, from, to);
    return  key.substring(1, key.length() - 1);
  }

  public static Object decodeBytes(String in){
   return decodeBytes(in.getBytes(), 0, in.getBytes().length, Syntax.STANDARD);
  }

  public static class Obj extends HashMap<String, Object> {

  }

  public static class Arr extends ArrayList {

  }

  public interface Syntax {

    Syntax STANDARD = new DefaultSyntax();

    char o1();
    char o2();
    char a1();
    char a2();
    char es();

    /**
     * numeric delimiter. For example '.' inside 123.345
     * @return
     */
    char nd();

    /**
     * numeric negation. For example '-' inside -44
     * @return
     */
    char ng();

    /**
     * key delimiter. For example ':' inside {'a': 1}
     * @return
     */
    char kd();

    /**
     * value delimiter. For example ',' inside a list like [1, 2, 3, 4]
     * @return
     */
    char vd();


    char []quotes();

    String vtrue();
    String vfalse();

    String vnull();

    boolean isWhitespace(byte b);

  }

  public static class DefaultSyntax implements Syntax {

    @Override
    public char o1() {
      return '{';
    }

    @Override
    public char o2() {
      return '}';
    }

    @Override
    public char a1() {
      return '[';
    }

    @Override
    public char a2() {
      return ']';
    }

    @Override
    public char es() {
      return '\\';
    }

    @Override
    public char nd() {
      return '.';
    }

    @Override
    public char ng() {
      return '-';
    }

    @Override
    public char kd() {
      return ':';
    }

    @Override
    public char vd() {
      return ',';
    }

    @Override
    public char[] quotes() {
      return new char[]{'\'', '"'};
    }

    @Override
    public String vtrue() {
      return "true";
    }

    @Override
    public String vfalse() {
      return "false";
    }

    @Override
    public String vnull() {
      return "null";
    }

    @Override
    public boolean isWhitespace(byte c) {
      return c == '\n' || c == '\t' || c == ' ';
    }


  }
}
