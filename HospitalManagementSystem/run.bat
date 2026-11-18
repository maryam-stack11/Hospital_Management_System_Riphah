@echo off
REM Compile and run the Hospital Management System (Windows)
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
start "" javaw -cp out Main
