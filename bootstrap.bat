@echo off
@rem Find java.exe
@rem set JAVA_HOME=C:\Program/ Files\Java\jdk1.7.0_80\bin

set JVEXE=%JAVA_HOME%\java.exe
set JVC=%JAVA_HOME%\javac.exe

IF EXIST out\weland-1.0.jar DEL /F out\weland-1.0.jar
"%JVC%" -d "build\classes" src\main\java\com\arise\Builder.java
"%JVEXE%" -cp "build\classes"  com.arise.Builder %1 ./
"%JVEXE%" -cp "out/weland-1.0.jar;libs\*" com.arise.weland.Main



