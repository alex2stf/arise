@echo off
@rem Find java.exe


set JVEXE="%JAVA_HOME%\bin\java.exe"
set JVC="%JAVA_HOME%\bin\javac.exe"

IF "%1"=="run-weland" (
    "%JVEXE%" -cp "out/weland-1.0.jar;libs\*" com.arise.weland.Main
) ELSE (
    "%JVC%" -d "build\classes" src\main\java\com\arise\Builder.java
    "%JVEXE%" -cp "build\classes"  com.arise.Builder %1 ./
)


