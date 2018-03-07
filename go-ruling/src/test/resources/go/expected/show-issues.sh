#!/usr/bin/env bash

set -euo pipefail

SRC_DIR="$( cd "$( dirname "$0" )" && cd ../../../ruling-test-sources && pwd )"

display_code() {
  local FILE_PATH="$1"
  echo "${FILE_PATH}"
  shift
  while [ $# != 0 ]; do
    local POS="$1"
    local LINE="${POS%:*}"
    local COLUMN="${POS#*:}"
    local LINE_BEFORE=$(( LINE - 4 ))
    ((LINE_BEFORE = LINE_BEFORE < 1 ? 1 : LINE_BEFORE))
    cat -n "${FILE_PATH}" | sed -ne "s|\t| |g" -e "${LINE_BEFORE},${LINE}p"

    local indent_count
    ((indent_count = COLUMN + 6))
    printf "%${indent_count}s"

    echo '^--'
    shift
  done
  echo
}

cat "$@" | while IFS= read -r line; do
    display_code "${SRC_DIR}/"${line/:/}
done
