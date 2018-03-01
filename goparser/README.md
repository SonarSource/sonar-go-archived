# goparser

## Building

To create `goparser` executable in current directory, run:

    go build

To create `goparser` executable in `$GOPATH/bin`, run:

    go install

## Running

If you have `$GOPATH/bin` on your `PATH`, it's easy to run with `goparser`.

Run with `-h` or `-help` or `--help` to get usage help.

Print the UAST for some `source.go`:

    goparser source.go

Dump the native raw AST for some `source.go`:

    goparser -d source.go

## Goodies

The `samples/` directory contains the AST and UAST.json files that correspond to the samples in `../go-samples`. You can regenerate them with:

    ./samples/gen.sh
