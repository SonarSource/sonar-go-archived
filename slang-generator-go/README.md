# uast-generator-go

## Building

To create `uast-generator-go` executable in current directory, run:

    go build

To create `uast-generator-go` executable in `$GOPATH/bin`, run:

    go install

## Running

If you have `$GOPATH/bin` on your `PATH`, it's easy to run with `uast-generator-go`.

Run with `-h` or `-help` or `--help` to get usage help.

Print the UAST for some `source.go`:

    uast-generator-go source.go

Dump the native raw AST for some `source.go`:

    uast-generator-go -d source.go
