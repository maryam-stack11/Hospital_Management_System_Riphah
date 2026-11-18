@echo off
REM Compile and run the Hospital Management System (Windows)
cd /d "%~dp0"
if not exist out mkdir out
pushd src
javac -d ..\out *.java
if errorlevel 1 (
    popd
    echo Compilation failed.
    pause
    exit /b 1
)
popd
java -cp out Main
pause
