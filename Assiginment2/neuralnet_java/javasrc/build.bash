#!/bin/bash
set -u -e
echo "Compiling..."
javac *.java
echo "Running..."
java Main
