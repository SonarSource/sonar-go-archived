#!/usr/bin/env bash

if ! type uast-generator-go &>/dev/null; then
    echo "The program 'uast-generator-go' doesn't exist or it's not on your path."
    echo "Add ~/go/bin to your PATH, and install uast-generator-go with 'go install'"
    exit 1
fi

cd $(dirname "$0")

for f in ../../go-ruling/src/test/resources/go/samples/*.go; do
    basename=$(basename "$f")
    uast-generator-go -d "$f" > "$basename.ast"
    json=$basename.uast.json
    if ! uast-generator-go "$f" > "$json" 2>/dev/null; then
        echo "Error: could not parse $f. Run uast-generator-go manually on it to investigate."
        rm -f "$json"
    fi
done
