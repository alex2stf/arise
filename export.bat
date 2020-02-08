@echo off
@rem Find java.exe

set JAVA_HOME="Jv7Win32SDK\Java\jdk1_7_0_79"
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set JAVAC_EXE=%JAVA_HOME%\bin\javac.exe
"%JAVAC_EXE%" -d "build\classes" src\main\java\com\arise\Builder.java
"%JAVA_EXE%" -cp "build\classes"  com.arise.Builder
java -cp "out/weland-1.0.jar;libs\*" com.arise.weland.Main