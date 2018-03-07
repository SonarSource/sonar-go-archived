#!/usr/bin/env bash

SRC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../../../ruling-test-sources && pwd )"

function display_code() {
  local FILE_PATH="$1"
  echo "${FILE_PATH}"
  shift
  while [ -n "$1" ]; do
    local POS="$1"
    local LINE="${POS%:*}"
    local COLUMN="${POS#*:}"
    local LINE_BEFORE=$(( $LINE - 4 ))
    LINE_BEFORE=$(( $LINE_BEFORE < 1 ? 1 : $LINE_BEFORE ))
    cat -n "${FILE_PATH}" | sed -n "${LINE_BEFORE},${LINE}p" | sed "s|\t| |g"
    for ((i=-5; i<=$COLUMN; i++)); do echo -n ' '; done
    echo '^--'
    shift
  done
  echo
}

source <(
  cat "$@" | sed "s|^|display_code \"${SRC_DIR}/|;s|:|\"|"
)
