#!/usr/bin/env bash

if ! type goparser &>/dev/null; then
    echo "The program 'goparser' doesn't exist or it's not on your path."
    echo "Add ~/go/bin to your PATH, and install goparser with 'go install' inside cmd/goparser"
    exit 1
fi

cd $(dirname "$0")

for f in ../../go-samples/*.go; do
    basename=$(basename "$f")
    goparser -d "$f" > "$basename.ast"
    json=$basename.uast.json
    if ! goparser "$f" > "$json" 2>/dev/null; then
        echo "Error: could not parse $f. Run goparser manually on it to investigate."
        rm -f "$json"
    fi
done
