# slang-generator-go

Generate slang serialized AST in JSON from a go source file.

## Building

To generate `goparser_generated.go` file in current directory, run:

    go generate

To create `slang-generator-go` executable in current directory, run:

    go build

To create `slang-generator-go` executable in `$GOPATH/bin`, run:

    go install

## Running

If you have `$GOPATH/bin` on your `PATH`, it's easy to run with `slang-generator-go`.

Run with `-h` or `-help` or `--help` to get usage help.

Print the SLANG Json tree for some `source.go`:

    slang-generator-go source.go

Dump the native raw AST for some `source.go`:

    slang-generator-go -d source.go
