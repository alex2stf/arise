package com.arise.core.serializers.parser;

import com.arise.core.serializers.parser.Whisker.Lambda;
import com.arise.core.serializers.parser.Whisker.Token;
import com.arise.core.serializers.parser.Whisker.TokenType;

import java.io.*;
import java.util.*;

import static com.arise.core.tools.Assert.assertEquals;


public class WhiskerTest {




  public void testNodeRead(){
    String input = "txt1 {{arg}} txt2 {{ #section_start}} {{@index}} {{.}} {{#non_false?}} {{>partial}} {{ !comment}} {{& unescaped_with_&}} text {{/tst}}1dasd dsad asdasd asdasd asdasd asdasd asdasdas dasdasd2{{{undescaped_html}}}---{{node}} tttttx";

    Whisker whisker = new Whisker();
    Token root = whisker.readNodes((input));

    assertEquals(TokenType.TEXT, root.next.tokenType);
    assertEquals(TokenType.PROP, root.next.next.tokenType);
    assertEquals(TokenType.SECTION_START, root.next.next.next.next.tokenType);

    Whisker.printNode(root);
  }

  public void testCompiler1(){
    String input = "property test: {{prop}}\n"
        + "{{!this line should be ignored}}\n"
        + "{Student}:\n"
        + "{{#student}}Name: {{name}}, class: {{clazz}} {{/student}}\n"
        + "Nothing to show here: {{#person}}Name: {{name}}, class: {{clazz}} {{/person}}!!!\n"
        + "Inherited naming: {{#name}}{{#name}}{{name}}{{/name}}{{/name}}\n"
        + "{{#person?}} no person defined {{/person?}}\n"
        + "EOF";

    Student student = new Student();
    student.name = "<<STUDENT_NAME>>";
    student.clazz = "<<STUDENT_CLASS>>";

    Name name = new Name();
    name.name = new Student();
    name.name.name = "No collisions!!!";

    Map map = new HashMap();
    map.put("prop", "<<PROPERTY_VALUE>>");
    map.put("student", student);
    map.put("name", name);

    String out = new Whisker().compile(input, map);

    assertEquals("property test: <<PROPERTY_VALUE>>\n"
        + "\n"
        + "{Student}:\n"
        + "Name: <<STUDENT_NAME>>, class: <<STUDENT_CLASS>> \n"
        + "Nothing to show here: !!!\n"
        + "Inherited naming: No collisions!!!", out);

  }

  public void testIterables(){

    List<Student> students = new ArrayList<>();
    Student[] array = new Student[5];

      for (int i = 0; i < 5; i++){
        Student s = new Student();
        s.name = "student_" + i;
        students.add(s);
        array[i] = s;
      }
    Map map = new HashMap();
    map.put("students", students);

    String input = "students:\n{{#students}}{{.}}) {{name}} \n{{/students}}";

    String out = new Whisker().compile(input, map);

    assertEquals("students:\n"
        + "0) student_0 \n"
        + "1) student_1 \n"
        + "2) student_2 \n"
        + "3) student_3 \n"
        + "4) student_4 \n", out);

    System.out.println(out);

    map = new HashMap();
    map.put("students", array);
    out = new Whisker().compile(input, map);

    assertEquals("students:\n"
        + "0) student_0 \n"
        + "1) student_1 \n"
        + "2) student_2 \n"
        + "3) student_3 \n"
        + "4) student_4 \n", out);
  }


  public void testManSample(){
    String input = "Hello {{name}}\n"
        + "You have just won {{value}} dollars!\n"
        + "{{#in_ca}}"
        + "Well, {{taxed_value}} dollars, after taxes."
        + "{{/in_ca}}";

    ManSample manSample = new ManSample();
    manSample.name = "Chris";
    manSample.value = 20;
    manSample.in_ca = true;
    manSample.taxed_value = (long) (10000 - (10000 * 0.4));


    String out = new Whisker().compile(input, manSample);

    assertEquals("Hello Chris\n"
        + "You have just won 20 dollars!\n"
        + "Well, 6000 dollars, after taxes.", out);
    System.out.println(out);

  }


  public void testLambda(){
    String input = "{{#wrapped}}{{name}} is awesome.{{/wrapped}}";

    Whisker whisker = new Whisker();

    Map context = new HashMap();
    context.put("name", "Willy");
    context.put("wrapped", new Lambda() {
      @Override
      public void apply(Token token, Writer writer, Object context, int index, Whisker compiler) {
        try {
          compiler.compileNodes(token, writer, context, index, null, null);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    String out = whisker.compile(input, context);
    assertEquals("Willy is awesome.", out);

  }

  public void testInvertedSection(){
    String input = "{{^repo}}No repos :({{/repo}}";
    Map context = new HashMap();

    String out;
    Whisker whisker = new Whisker();

    out = whisker.compile(input, context);
    assertEquals("No repos :(", out);

    context.put("repo", new HashSet<>());
    out = whisker.compile(input, context);
    assertEquals("No repos :(", out);

    context.put("repo", new String[]{"tst"});

    out = whisker.compile(input, context);
    assertEquals("", out);
    System.out.println(out);
  }

  public void testHashIterations(){
    String input = "Init map: {{#map}} map exists!!!\nunu={{unu}}, doi={{doi}} start iteration:\n{{#.}}Name: index: {{@index}} key: {{@key}} value:{{@value}}\n{{/.}}\niteration finished{{/map}}";
    Map context = new HashMap();
    Map map = new HashMap();
    map.put("unu", 1);
    map.put("doi", 2);
    map.put("name", "flow");
    context.put("map", map);

    Whisker whisker = new Whisker();

    String out;
    out = whisker.compile(input, context);

    assertEquals("Init map:  map exists!!!\n"
        + "unu=1, doi=2 start iteration:\n"
        + "Name: index: 0 key: name value:flow\n"
        + "Name: index: 1 key: unu value:1\n"
        + "Name: index: 2 key: doi value:2\n"
        + "\n"
        + "iteration finished", out);

    input = "Iterating over a string: {{#myString}}\n{{#.}} {{@key}} {{@index}} {{@value}}\n{{/.}} {{/myString}}";

    context.put("myString", "hello world");
    out = whisker.compile(input, context);


    assertEquals("Iterating over a string: \n"
        + " h 0 h\n"
        + " e 1 e\n"
        + " l 2 l\n"
        + " l 3 l\n"
        + " o 4 o\n"
        + "   5  \n"
        + " w 6 w\n"
        + " o 7 o\n"
        + " r 8 r\n"
        + " l 9 l\n"
        + " d 10 d\n ", out);

  }

  public void testCustomDelimiters(){
    String input = "[[prop1]] and [[{unescaped_prop}]]";

    Whisker whisker = new Whisker().setStartDelimiter("[[").setEndDelimiter("]]");

    Map map = new HashMap();
    map.put("prop1", "property");
    map.put("unescaped_prop", "unescaped property");
    String out = whisker.compile(input, map);

    assertEquals("property and unescaped property", out);

    input = "{{prop1}} and {{{unescaped_prop}}}";
    out = whisker.setStartDelimiter("{{").setEndDelimiter("}}").compile(input, map);

    assertEquals("property and unescaped property", out);
  }

  public void testSetDelimiters(){
    String input = "{{property}} {{=<%%>=}} <% property %> <%={{ }}=%> {{property}}";
    Map map = new HashMap();
    map.put("property", "test");

    String out = new Whisker().compile(input, map);

    assertEquals("test  test  test", out);
  }

  public void testGetters(){
    String input = "abc{{#item}} {{scale}} {{/item}}def";
    Map map = new HashMap();
    map.put("item", new GXZ());
    String out = new Whisker().compile(input, map);
    assertEquals("abc 234xxt def", out);
  }



  public void testStreams() throws IOException {

    String input = "abc{{#item}} {{scale}} {{/item}}def";
    Map map = new HashMap();
    map.put("item", new GXZ());


    InputStream targetStream = new ByteArrayInputStream(input.getBytes());
    OutputStream outputStream = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(outputStream);


    new Whisker().compile(new InputStreamReader(targetStream), writer, new HashMap<>());

    /**this should not be part of whisker, without writer#close nothing will be displayed**/
    writer.close();
    outputStream.close();
    targetStream.close();
    System.out.println(outputStream);
  }

  public void testPartials() throws IOException {

    Map map = new HashMap();
    map.put("item", new GXZ());

    String out = new Whisker().setTemplatesRoot("src/test/resources#_whisker_")
            .setExtension(".htm")
            .compile("test {{> partial}} ", map);
    assertEquals("test SOME PARTIAL IN HERE  234xxt", out.trim());

    out = new Whisker().setTemplatesRoot("src/test/resources#_whisker_")
            .setExtension(".htm")
            .compileTemplate("main", map);
    assertEquals("MAIN 111 SOME PARTIAL IN HERE  234xxt  |", out);
  }




  class ManSample {
    String name;
    int value;
    boolean in_ca;
    public long taxed_value;
  }



  class Student {
    String name;
    String clazz;
    public String getClazz() {
      return clazz;
    }
    public String getName() {
      return name;
    }
  }

  class GXZ{
    public String getScale(){
      return "234xxt";
    }
  }

  class Name {
    Student name;
  }
}