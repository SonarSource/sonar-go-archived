# External Linter Reports

## go vet

The file `go-vet.out` has been generated using the command:
```
go vet 2> go-vet.out || [ $? == 1 ]
```

## Golint

The file `golint.out` has been generated using the command:
```
golint > go-lint.out
```

## GoMetaLinter

The file `gometalinter.out` has been generated using the command:
```
gometalinter SelfAssignement.go > gometalinter.out
```
