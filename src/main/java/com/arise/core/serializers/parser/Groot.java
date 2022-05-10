package com.arise.core.serializers.parser;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.TypeUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.TypeUtil.IteratorHandler;
import static com.arise.core.tools.TypeUtil.isNull;
import static com.arise.core.tools.TypeUtil.isNumber;
import static com.arise.core.tools.TypeUtil.isNumericSequence;
import static com.arise.core.tools.TypeUtil.toNumber;
import static java.lang.String.valueOf;

public class Groot {

  public static Object decodeBytes(byte[] in, int firstIndex, int lastIndex, Syntax s){
    //TODO improve this


    while (s.isWhitespace(in[firstIndex])){
      firstIndex++;
    }

    while (s.isWhitespace(in[lastIndex])){
      lastIndex--;
    }




    if (isMatch(in, firstIndex, lastIndex + 1, s.vtrue())){
      return true;
    }


    if (isMatch(in, firstIndex, lastIndex + 1, s.vnull())){
      return null;
    }



    if (isMatch(in, firstIndex, lastIndex + 1, s.vfalse())){
      return false;
    }


    if (isNumericSequence(in, firstIndex, lastIndex + 1, s.nd(), s.ng())){
      return toNumber(in, firstIndex, lastIndex  + 1, s.nd(), s.ng());
    }


    for (char c: s.quotes()){
      //TODO check quotes are not escaped
      if (in[firstIndex] == c && in[lastIndex] == c){
        return decodeString(in, firstIndex + 1, lastIndex);
      }
    }


    int prevFrom = (firstIndex > 0) ? firstIndex -1 : firstIndex;
    int prevLast = (lastIndex > 0) ? lastIndex -1 : lastIndex;
    char prevF = (char) in[prevFrom];
    char prevL = (char) in[prevLast];
    char firstC = (char) in[firstIndex];
    char lastC = (char) in[lastIndex];

//    if ((char)in[firstIndex] == s.o1() && prevFrom != s.es() && prevLast != s.es() && (char)in[lastIndex] == s.o2()){
    if (firstC == s.o1() && lastC == s.o2()){
      return decodeObject(in, firstIndex + 1, lastIndex  - 1, s);
    }

    if (firstC == s.a1() && lastC == s.a2()){
      return decodeArray(in, firstIndex + 1, lastIndex - 1, s);
    }

    throw new RuntimeException("Invalid <" + firstC + "..." + lastC + ">");

  }



  public static String toJson(Object obj) {
    return  toJson(obj, Syntax.STANDARD);
  }

  public static String toJson(Object obj, Syntax s){
    GRiter gRiter = new GRiter(new StringWriter(), s);
    toJson(obj, gRiter);
    return gRiter.close().toString();
  }

  public static void toJson(Object obj, final GRiter gRiter) {

    if (isNull(obj)){
      gRiter.writeNull();
    }
    else if (isNumber(obj)){
      gRiter.writeNumber(obj);
    }

    else if ("true".equals(valueOf(obj)) ){
      gRiter.writeBoolean(true);
    }

    else if ("false".equals(valueOf(obj)) ){
      gRiter.writeBoolean(false);
    }

    else if (obj instanceof CharSequence){
      gRiter.writeString(obj);
    }

    else {
      final boolean isArray = TypeUtil.isSingleKeyIterable(obj);


      if (isArray){
        gRiter.writeArrayStart();
      } else {
        gRiter.writeObjectStart();
      }


      final int[] i = {-1};
      TypeUtil.forEach(obj, new IteratorHandler() {
        @Override
        public void found(Object key, Object value, int index) {

          i[0]++;
          if (i[0] > 0){
            gRiter.write(",");
          }

          if (isArray){
            toJson(value, gRiter);
          } else {
            gRiter.write("\"")
                    .write(StringUtil.jsonEscape(valueOf(key)))
                    .write("\":");
            toJson(value, gRiter);
          }
        }

      }, false);

      if (isArray){
        gRiter.writeArrayEnd();
      } else {
        gRiter.writeObjectEnd();
      }

    }//exit else
  }




  static class GRiter {
    private final Writer writer;
    private final Syntax s;

    public GRiter(Writer writer, Syntax syntax){
      this.writer = writer;
      this.s = syntax;
    }

    public GRiter close(){
      try {
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return this;
    }

    public GRiter writeNull() {
      return write("null");
    }

    public GRiter writeNumber(Object obj) {
      return write(obj.toString());
    }

    private GRiter write(String bytes){
      try {
        writer.write(bytes);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return this;
    }

    private GRiter write(char c){
      try {
        writer.write(c);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return this;
    }

    public GRiter writeBoolean(Object obj) {
      boolean b = (boolean) obj;
      if (b){
        return write(s.vtrue());
      }
      return write(s.vfalse());
    }

    public GRiter writeString(Object obj) {
      return write( "\"" + StringUtil.jsonEscape((String) obj) + "\"");
    }

    public GRiter writeArrayStart() {
      return write(s.a1());
    }

    public GRiter writeObjectStart() {
      return write(s.o1());
    }

    @Override
    public String toString() {
      return writer.toString();
    }

    public GRiter writeArrayEnd() {
      return write(s.a2());
    }

    public GRiter writeObjectEnd() {
      return write(s.o2());
    }
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

    return x == match.length();
  }

  private static String decodeString(byte[] in, int from, int to){
    StringBuilder sb = new StringBuilder();
    for (int i = from; i < to; i++){
      sb.append((char)in[i]);
    }
    return sb.toString();
  }

  private static Object decodeArray(byte[] in, int from, int to, Syntax s) {

    List response = new ArrayList();

    BufHelper helper = new BufHelper(s);
    int cursor = from;

    for (int i = from; i < to; i++){
      char c = (char) in[i];
      helper.track(c);

      if (helper.isSafe() && c == s.vd()){
        Object object = decodeBytes(in, cursor, i - 1 , s);
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


    Map response = new HashMap();


    int cursor = from;
    BufHelper helper = new BufHelper(s);

    String key = null;

    for (int i = from; i < to; i++){
      char c = (char) in[i];
      helper.track(c);

      if (helper.isSafe()){
          if (c == s.kd()){
            key = (String) decodeBytes(in, cursor, i - 1, s);
            cursor = i + 1;
          }
          else if (c == s.vd()){
            Object obj = decodeBytes(in, cursor, i - 1, s);
            response.put(key, obj);
            cursor = i + 1;
          }
      }
    }


    if (key != null) {
      Object obj = decodeBytes(in, cursor, to , s);
      response.put(key, obj);
    }
    return response;
  }


  private static String debug(String prefix, byte[] bytes, int from, int to){
//    StringBuilder full = new StringBuilder();
    StringBuffer sb = new StringBuffer();
    for (int i = from; i < to; i++){
      sb.append((char)bytes[i]);
    }

//    System.out.println(" FULL " + new String(bytes));
    System.out.println(prefix + "  (" + from + " to " + to + ")=" + sb.toString());
    return sb.toString();
  }




  public static Object decodeBytes(String in){
   return decodeBytes(in.getBytes(), 0, in.getBytes().length - 1, Syntax.STANDARD);
  }

  public static Object decodeFile(File in) throws IOException {
    String content = FileUtil.read(in);
    content = content.replaceAll("\\s+", " ");
    return Groot.decodeBytes(content);
  }

  public static Object decodeBytes(byte in[]){
    return decodeBytes(in, 0, in.length - 1, Syntax.STANDARD);
  }

//  public static class Obj extends LinkedHashMap<String, Object> {
//
//    public String getString(String name) {
//      return String.valueOf(get(name));
//    }
//  }
//
//  public static class Arr extends ArrayList {
//
//  }

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
      return new char[]{'"'};
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
      return c == '\n' || c == '\t' || c == ' ' || c == '\r';
    }


  }


}
