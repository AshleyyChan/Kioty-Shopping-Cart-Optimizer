@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Building KIOTY Java Optimizer Backend
echo ============================================

cd /d "%~dp0"

if not exist bin mkdir bin

echo Compiling Java source files...
javac -encoding UTF-8 -d bin src\main\java\com\kioty\model\*.java src\main\java\com\kioty\algorithm\*.java src\main\java\com\kioty\util\*.java src\main\java\com\kioty\service\*.java src\main\java\com\kioty\App.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

echo Packaging kioty-optimizer.jar...
jar --create --file kioty-optimizer.jar --main-class com.kioty.App -C bin .

echo ============================================
echo   Build Successful!
echo   Run 'run.bat' to start the server.
echo ============================================
