#!/bin/bash
# Compile and run the Hospital Management System (Linux / macOS / Git Bash)
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -encoding UTF-8 -d out src/*.java || { echo "Compilation failed."; exit 1; }
java -cp out Main
