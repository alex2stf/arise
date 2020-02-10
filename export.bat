@echo off
@rem Find java.exe

set JVHOME="Jv7Win32SDK\Java\jdk1_7_0_79"
set JVEXE=%JAVA_HOME%\jre\bin\java.exe
set JVC=%JAVA_HOME%\bin\javac.exe
"%JVC%" -d "build\classes" src\main\java\com\arise\Builder.java
"%JVEXE%" -cp "build\classes"  com.arise.Builder
"%JVEXE%" -cp "out/weland-1.0.jar;libs\*" com.arise.weland.Main