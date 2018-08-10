#!/bin/bash
set -e
javac Game.java Model.java View.java Controller.java
echo "Build complete. To run, do:"
echo "    java Game 0 100 10"
echo "and start tapping keys or your mouse button."
