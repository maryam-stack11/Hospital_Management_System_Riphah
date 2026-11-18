@echo off
cd /d "%~dp0\.."
if not exist out mkdir out
javac -encoding UTF-8 -cp out -d out src\*.java tests\HospitalTests.java
if errorlevel 1 exit /b 1
java -cp "out" HospitalTests
pause
