#!/usr/bin/env bash
set -euf -o pipefail

PROJECT="$( cd "$( dirname "$0" )" && cd .. && pwd )"
case "$(uname -s)" in
    Darwin*)          GO_PARSER="$PROJECT/uast-generator-go/build/uast-generator-go-darwin-amd64";;
    MINGW* | CYGWIN*) GO_PARSER="$PROJECT/uast-generator-go/build/uast-generator-go-windows-amd64.exe";;
    *)                GO_PARSER="$PROJECT/uast-generator-go/build/uast-generator-go-linux-amd64"
esac
"$GO_PARSER" "$@"
