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

# Test Report

The file `go-test-report.out` has been generated in this context:

* go version 1.10
* GOPATH defined so the sonar-go git repository is in $GOPATH/src/github.com/SonarSource/sonar-go

Using the command:
```
go1.10 test -json | \
 sed 's|github.com/SonarSource/sonar-go/its/plugin/projects/samples|samples|g' > go-test-report.out
```
