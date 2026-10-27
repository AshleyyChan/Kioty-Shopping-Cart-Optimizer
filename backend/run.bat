@echo off
setlocal

cd /d "%~dp0"

if not exist bin\com\kioty\App.class (
    echo Binaries not found. Running build first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

echo Starting KIOTY Java Optimizer on port %PORT% (default 5050)...
java -cp bin com.kioty.App
