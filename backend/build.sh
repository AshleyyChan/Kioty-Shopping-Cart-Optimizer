#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

mkdir -p bin

echo "Compiling Java sources..."
javac -encoding UTF-8 -d bin $(find src/main/java -name "*.java")

echo "Packaging kioty-optimizer.jar..."
jar --create --file kioty-optimizer.jar --main-class com.kioty.App -C bin .

echo "Build complete."
