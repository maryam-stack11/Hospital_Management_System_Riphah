#!/bin/bash
# Compiles everything and runs the automated business-rule tests.
cd "$(dirname "$0")/.."
mkdir -p out
javac -d out src/*.java tests/HospitalTests.java && java -cp out HospitalTests
