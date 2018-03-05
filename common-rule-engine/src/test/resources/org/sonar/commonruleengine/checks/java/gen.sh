#!/usr/bin/env bash

SCRIPT=$(readlink -f "$0")
SCRIPT_DIR=$(dirname ${SCRIPT})

gradle --console=plain --no-daemon --quiet :uast-generator-java:generateUast "-PinputFile=${SCRIPT_DIR}"
