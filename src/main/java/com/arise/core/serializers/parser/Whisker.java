package com.arise.core.serializers.parser;

import com.arise.core.exceptions.LogicalException;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.models.FilterCriteria;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.GenericTypeWorker;
import com.arise.core.tools.TypeUtil;
import com.arise.core.tools.TypeUtil.IteratorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.arise.core.tools.ReflectUtil.hasAnyOfTheAnnotations;
import static com.arise.core.tools.StringUtil.trim;
import static com.arise.core.tools.TypeUtil.getField;
import static com.arise.core.tools.TypeUtil.isSingleKeyIterable;

public class Whisker extends GenericTypeWorker {


  public Whisker(){
    setMethodCriteria(new FilterCriteria<Method>() {
      @Override
      public boolean isAcceptable(Method m) {
        return m != null && !Modifier.isStatic(m.getModifiers())
                && !hasAnyOfTheAnnotations(m, TypeUtil.jsonIgnoreAnnotations);
      }
    });
  }

  private char s1 = '{';
  private char s2 = '{';
  private char e1 = '}';
  private char e2 = '}';
  private String tplRoot = "";
  private String defaultExt = "";


  public Whisker setStartDelimiter(String in){
    s1 = in.charAt(0);
    s2 = in.charAt(1);
    return this;
  }

  public Whisker setEndDelimiter(String in){
    e1 = in.charAt(0);
    e2 = in.charAt(1);
    return this;
  }

  @Override
  public String toString() {
    return s1 + ""  + s2 + " " + e1 + e2;
  }

  public Token readNodes(String in){
    DigestResult result = new DigestResult();
    for(int i = 0; i < in.length(); i++){
      result = digest(in.charAt(i), result);
    }
    if (result.sb.length() > 0) {
      result.add(new Token(result.sb.toString(), this));
    }
    return result.root;
  }

  public Token readNodes(Reader reader) throws IOException {
    int r;

    DigestResult result = new DigestResult();
    while ((r = reader.read()) != -1) {
      result = digest((char) r, result);
    }
    if (result.sb.length() > 0) {
      result.add(new Token(result.sb.toString(), this));
    }

    return result.root;
  }

  public DigestResult digest(char c, DigestResult dig){

    if (dig.lwe){
      dig.lwe = false;
      return dig;
    }

    dig.sb.append(c);
    if (dig.sb.length() < 2){
      return dig;
    }
    char a = dig.sb.charAt(dig.sb.length() - 2);
    char b = dig.sb.charAt(dig.sb.length() - 1);




    if (a == s1 && b == s2) {

      //skip newline for tag end
      String nextText = dig.sb.substring(0, dig.sb.length() - 2);
      if (dig.selected.tokenType.equals(TokenType.SECTION_END) && nextText.endsWith("\n")){
        nextText = nextText.substring(0, nextText.length() - 1);
      }

      dig.add(new Token( nextText, TokenType.TEXT));
      dig.sb = new StringBuilder();
      dig.lwe = false;
    }
    else if (a == e1 && b == e2) {
      dig.lwe = false;
      String txt = dig.sb.substring(0, dig.sb.length() - 2);
      if (txt.length() > 5 && txt.charAt(0) == '=' && txt.charAt(txt.length() - 1) == '='){
        s1 = txt.charAt(1);
        s2 = txt.charAt(2);
        e2 = txt.charAt(txt.length() - 2);
        e1 = txt.charAt(txt.length() - 3);
        dig.sb = new StringBuilder();
      }
      else if ('{' == txt.charAt(0)){
        dig.lwe = true;
        txt = txt.substring(1, txt.length());
        if (!escapeIsSame() && txt.charAt(txt.length() -1 ) == '}'){
          txt = txt.substring(0, txt.length() - 1);
        }
        dig.add(new Token(txt, TokenType.UNESCAPED));
      }
      else {
        Token nextToken = new Token(dig.sb.toString(), this);
        //skip newline for tag end
        if (dig.selected.tokenType.equals(TokenType.SECTION_END) && nextToken.t.endsWith("\n") && nextToken.tokenType.equals(TokenType.TEXT)){
          nextToken.t = nextToken.t.substring(0, nextToken.t.length() - 1);
        }
        dig.add(nextToken);
      }

      dig.sb = new StringBuilder();
    }
    return dig;
  }

  private boolean escapeIsSame(){
    return e1 == '}' && e2 == '}';
  }

  public static void printNode(Token token){
    System.out.println(token.tokenType + " " + token.t);
    if (token.next != null){
      printNode(token.next);
    }
  }


  /**
   * this method is not closing the writer
   * @param reader
   * @param writer
   * @param context
   * @throws IOException
   */
  public void compile(Reader reader, Writer writer, Object context) throws IOException {
    Token token = readNodes(reader);
    if (!token.tokenType.equals(TokenType.ROOT)){
      throw new SyntaxException("First token must be ROOT");
    }
//    printNode(token);
    compileFromFirstToken(token, writer, context);
  }


  /**
   * @param reader
   * @param context
   * @throws IOException
   */
  public String compile(Reader reader, Object context) throws IOException {
    Token token = readNodes(reader);
    if (!token.tokenType.equals(TokenType.ROOT)){
      throw new SyntaxException("First token must be ROOT");
    }
    StringWriter stringWriter = new StringWriter();
    compileFromFirstToken(token, stringWriter, context);
    stringWriter.close();
    return stringWriter.toString();
  }


  public String compileTemplate(String templateId, Object context){
    StringWriter stringWriter = new StringWriter();
    compileTemplate(templateId, stringWriter, context);
    try {
      stringWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return stringWriter.toString();
  }

  public void compileTemplate(String templateId, Writer writer, Object context){
    InputStream inputStream = FileUtil.findStream(tplRoot + templateId + defaultExt);
    try {
      compile(new InputStreamReader(inputStream), writer, context);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String compile(String input, Object context) {
    Token token = readNodes(input);
    if (!token.tokenType.equals(TokenType.ROOT)){
      throw new SyntaxException("First token must be ROOT");
    }
    StringWriter stringWriter = new StringWriter();
    compileFromFirstToken(token, stringWriter, context);
    try {
      stringWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return stringWriter.toString();
  }

  private void compileFromFirstToken(Token token, Writer writer, Object ctx){
    try {
      compileNodes(token.next, writer, ctx, 0, null, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String compilePartial(String partial, Object ctx){
    partial = trim(partial);
    String path = tplRoot + partial + defaultExt;
    InputStream inputStream = FileUtil.findStream(path);
    StringWriter stringWriter = new StringWriter();
    try {
      compile(new InputStreamReader(inputStream), stringWriter, ctx);
    } catch (Exception e) {
      throw new LogicalException("Failed to read partial " + path, e);
    }
    try {
      stringWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return stringWriter.toString();

  }

  public void compileNodes(Token token, final Writer writer, final Object context, int index, Object key, final Object value) throws IOException {

    switch (token.tokenType){
      case COMMENT: //ignored by definition
        break;
      case SECTION_END:
        //treated by search, just exit compilation
        return;
      case KEY:
        writer.write(String.valueOf(key));
        break;
      case VALUE:
        writer.write(String.valueOf(value));
        break;
      case TEXT:
        writer.write(token.t);
        break;
      case INDEX:
        writer.write(String.valueOf(index));
        break;
      case PROP:
        writer.write(String.valueOf(getValue(token.t, context)));
        break;
      case UNESCAPED:
        writer.write(String.valueOf(getValue(token.t, context)));
        break;
      case PARTIAL:
        writer.write(compilePartial(token.t, context));
        break;

      case INVERTED_SECTION: {
        Object newContext = getValue(token.t, context);
        Token tokenToCompile = token.next;
        token.next = searchEnd(tokenToCompile, token.t, 1, TokenType.SECTION_END, TokenType.INVERTED_SECTION);
        if (TypeUtil.invert(newContext)){
          compileNodes(tokenToCompile.clone(), writer, context, index, key, value);
        }
      }
      break;


      case ITERATION_SECTION: {

        final Token tokenToCompile = token.next;
        token.next = searchEnd(tokenToCompile, token.t, 1, TokenType.SECTION_END, TokenType.ITERATION_SECTION);

        TypeUtil.forEach(context, new IteratorHandler() {
          @Override
          public void found(Object localKey, Object localValue, int index) {
            try {
              compileNodes(tokenToCompile, writer, context, index, localKey, localValue);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });

      }
      break;

      case SECTION_START:


        Object newContext = getValue(token.t, context);
        final Token tokenToCompile = token.next;
        token.next = searchEnd(tokenToCompile, token.t, 1, TokenType.SECTION_END, TokenType.SECTION_START);

        //if context is just a boolean check, keep original context
        if (TypeUtil.isBooleanTrue(newContext) ){
          compileNodes(tokenToCompile, writer, context, index, key, value);
        }

        else if (newContext != null) {


          if (newContext instanceof Lambda){
            Lambda lambda = (Lambda) newContext;
            lambda.apply(tokenToCompile, writer, context, index, this);
          }


          //iterables and arrays
          else if (isSingleKeyIterable(newContext)){
            TypeUtil.forEach(newContext, new IteratorHandler() {
              @Override
              public void found(Object key, Object localContext, int index) {
                try {
                  compileNodes(tokenToCompile.clone(), writer, localContext, index, key, value);
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            });
          }



          //objects
          else {
            //string check:
//            if ((newContext instanceof String || newContext instanceof Boolean) && !TypeUtil.isBooleanTrue(newContext)){
            if ((newContext instanceof String || newContext instanceof Boolean) && !TypeUtil.isBooleanTrue(newContext)){
              ;;//TODO improve this

//              System.out.println("|||||||||||||||| THIS SHIT THROWS ERRORS!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            else {
              compileNodes(tokenToCompile, writer, newContext, index, key, value);
            }
          }


        }

        break;
    }


    if (token.next != null){
      compileNodes(token.next, writer, context, index, key, value);
    }
  }











  Token searchEnd(Token token, String name, int count, TokenType finish, TokenType init){

    if (token == null){
      throw new SyntaxException("Unmatched end section for " + name);
    }

    Token current = token.next;
    int skips = 0;
    while(current != null){
//      System.out.println("search end for " + name);
      if (init.equals(current.tokenType) && current.t.equals(name)){
        //new node, skip next finish
        skips++;
      }
      else if (finish.equals(current.tokenType) && current.t.equals(name)){
        skips--;
        if (skips <= 0){

          Token result = current.next;
          current.next = null;
          return result;
        }

      }
      current = current.next;
    }

    return current;
//    if (token == null){
//      throw new SyntaxException("Unmatched end section for " + name);
//    }
//    if (token.t.equals(name) && token.tokenType.equals(finish)){
//
//      if (finish.equals(token.tokenType)){
//        count--;
//      }
//      else if(refreshUI.equals(token.tokenType)){
//        count++;
//      }
//
//      if (count == 0){
//        Token result = token.next;
//        token.next = null;
//        return result;
//      }
//    }
//    return searchEnd(token.next, name, count, finish, refreshUI);
  }



  private Object getValue(String name, Object context){
    if (context == null){
      throw new SyntaxException("NULL context for " + name);
    }

    return getField(name, context, getFieldCriteria(), getMethodCriteria());
  }

  public Whisker setTemplatesRoot(String s) {
    this.tplRoot = s;
    if (!s.endsWith("/")){
      this.tplRoot += "/";
    }
    return this;
  }

  public Whisker setExtension(String s){
    this.defaultExt = s;
    return this;
  }

  public String findAndCompileStream(String s, Object context) throws IOException {
    InputStream inputStream = FileUtil.findStream(s);
    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

    return compile(inputStreamReader, context);
  }


  private class DigestResult {
    StringBuilder sb = new StringBuilder();
    boolean lwe = false;
    Token root = new Token("", TokenType.ROOT);

    Token selected;

    DigestResult(){
      selected = root;
    }

    void add(Token token){
      selected.next = token;
      selected = token;
    }
  }


  public interface Lambda {
    void apply(Token token, Writer writer, Object context, int index, Whisker compiler);
  }

  boolean endsWithEndDelimiters(String s){
    if (s.length() > 1){
      int x = s.length();
      return s.charAt(x - 1) == e2 && s.charAt( x - 2) == e1;
    }
    return false;
  }

  public static class Token {

    final TokenType tokenType;
    String t;

    Token next;

    private Token(String in, Whisker compiler) {



      String cp = in.trim();
      if (compiler.endsWithEndDelimiters(cp)){
        cp = cp.substring(0, cp.length() -2).trim();


        if ("@index".equals(cp) || ".".equals(cp)){
          tokenType = TokenType.INDEX;
        }
        else if ("@key".equals(cp)){
          tokenType = TokenType.KEY;
        }
        else if ("@value".equals(cp)){
          tokenType = TokenType.VALUE;
        }
        else {
          switch (cp.charAt(0)){
            case '#':
              if ("#.".equals(cp)){
                tokenType = TokenType.ITERATION_SECTION;
              }
              else {
                tokenType = TokenType.SECTION_START;
              }
              break;
            case '/':
              tokenType = TokenType.SECTION_END;
              break;
            case '&':
              tokenType = TokenType.UNESCAPED;
              break;
            case '>':
              tokenType = TokenType.PARTIAL;
              break;
            case '!':
              tokenType = TokenType.COMMENT;
              break;
            case '^':
              tokenType = TokenType.INVERTED_SECTION;
              break;
            default:
              tokenType = TokenType.PROP;
              break;
          }
        }



        if (!TokenType.PROP.equals(tokenType)){
          cp = cp.substring(1);
        }
        this.t = cp;

      } else {
        tokenType = TokenType.TEXT;
        this.t = in;
      }



    }

    private Token(String in, TokenType tokenType){
      this.tokenType = tokenType;
      this.t = in;
    }

    public Token clone(){
      Token token = new Token(this.t, this.tokenType);
      if (this.next != null){
        token.next = this.next.clone();
      }
      return token;
    }


  }


  enum TokenType {
    ROOT,
    TEXT,
    PROP, //{{prop}}
    SECTION_START, //{{#prop}}
    SECTION_END, //{/prop}}
    INVERTED_SECTION, // {{^prop}}
    ITERATION_SECTION,
    COMMENT, // {{! ignore me}}
    PARTIAL, // {{> ignore me}}
    INDEX, //{{@index}} or {{.}}
    KEY, //{{@key}}
    VALUE, //{{@value}}
    UNESCAPED //{{& prop}} or {{{prop}}}
  }

}
