#!/usr/bin/env bash
#
# SonarQube Go Plugin
# Copyright (C) 2018-2019 SonarSource SA
# mailto:info AT sonarsource DOT com
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#


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
    echo "Generating .uast.json for $f ..."
    json=$f.uast.json
    if ! "$GO_PARSER" "$f" > "$json" 2>/dev/null; then
        echo "Error: could not parse $f. Run $GO_PARSER manually on it to investigate."
        rm -f "$json"
    fi
done
