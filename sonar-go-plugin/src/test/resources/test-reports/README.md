### Test Report generation

The content of this directory has been generated using go 1.10.
Tests are run in the `uast-generator-go` directory.

#### Test pass, GOPATH is valid
```bash
go test -json 2>&1 > ../sonar-go-plugin/src/test/resources/test-reports/relative-path-pass.json
```

#### Test fail for Test_mapFile, GOPATH is valid
```bash
go test -json 2>&1 > ../sonar-go-plugin/src/test/resources/test-reports/relative-path-fail.json
```

#### Test pass, GOPATH is not valid
```bash
go test -json 2>&1 > ../sonar-go-plugin/src/test/resources/test-reports/absolute-path-pass.json
```
