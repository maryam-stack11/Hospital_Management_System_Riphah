#!/bin/bash
# Compiles everything and runs the automated business-rule tests.
cd "$(dirname "$0")/.."
mkdir -p out
javac -encoding UTF-8 -cp out -d out src/*.java tests/HospitalTests.java && java -cp out HospitalTests
