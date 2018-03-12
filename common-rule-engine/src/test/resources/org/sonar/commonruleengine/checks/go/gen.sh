#!/usr/bin/env bash

set -euo pipefail

PROJECT=$(cd "$(dirname "$0")" && cd "${PWD%%/sonar-go/*}/sonar-go" && pwd)
GO_PARSER_NAME=uast-generator-go
GO_PARSER_OUT=$PROJECT/$GO_PARSER_NAME/build
case "$(uname -s)" in
    Darwin*)          GO_PARSER=$GO_PARSER_OUT/$GO_PARSER_NAME-darwin-amd64 ;;
    MINGW* | CYGWIN*) GO_PARSER=$GO_PARSER_OUT/$GO_PARSER_NAME-windows-amd64.exe ;;
    *)                GO_PARSER=$GO_PARSER_OUT/$GO_PARSER_NAME-linux-amd64
esac

if ! [ -f "$GO_PARSER" ]; then
    echo "Error: the Go parser executable is missing: $GO_PARSER"
    echo "Make sure to build the project '$GO_PARSER_NAME'"
    exit 1
fi

cd "$(dirname "$0")"

shopt -s nullglob

for f in */*.go; do
    echo "Generating .ast and .uast.json for $f ..."
    json=$f.uast.json
    if ! "$GO_PARSER" "$f" > "$json" 2>/dev/null; then
        echo "Error: could not parse $f. Run $GO_PARSER manually on it to investigate."
        rm -f "$json"
    fi
done
