#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

if [ ! -f "bin/com/kioty/App.class" ]; then
    bash build.sh
fi

java -cp bin com.kioty.App
