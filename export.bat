@echo off
@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
set JAVAC_EXE=javac.exe

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
set JAVAC_EXE=%JAVA_HOME%/bin/javac.exe


if exist "%JAVAC_EXE%" goto init

if not exist "%JAVAC_EXE%" (
	if not exist "%JAVAC_EXE%" (
		echo.
		echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
		echo.
		echo Please set the JAVA_HOME variable in your environment to match the
		echo location of your Java installation.
		exit /b 1
	)
)




:init
if not exist "build\classes" mkdir "build\classes"
@rem init
"%JAVAC_EXE%" -d "build\classes" src\main\java\com\arise\Builder.java
"%JAVA_EXE%" -cp "build\classes"  com.arise.Builder
