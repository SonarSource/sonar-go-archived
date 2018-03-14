#!/usr/bin/env bash
set -euf -o pipefail

PROJECT="$( cd "$( dirname "$0" )" && cd .. && pwd )"
RULE_ENGINE_JAR="$PROJECT/common-rule-engine/build/libs/common-rule-engine-all.jar"
GO_PARSER="$PROJECT/scripts/go-parser.sh"
RULE_ENGINE_OPTIONS=""

scan_file() {
  GO_FILE="$1"
  java -jar "$RULE_ENGINE_JAR" ${RULE_ENGINE_OPTIONS} <( "$GO_PARSER" "$GO_FILE" ) "${GO_FILE}"
}

for param in "$@"; do
  if [ "${param:0:2}" == "--" ]; then
    RULE_ENGINE_OPTIONS="${RULE_ENGINE_OPTIONS} ${param}"
  fi
done

echo
for param in "$@"; do
  if [ "${param:0:2}" != "--" ]; then
    find "${param}" -type f -name "*.go" | while IFS= read -r line; do
      scan_file "${line}"
    done
  fi
done
