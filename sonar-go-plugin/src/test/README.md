# External Linter Reports

## go vet

To generate file `all-govet-report`:

- Clone [golang repository locally](https://github.com/golang/go) (commit 3d6e4ec0a8c2ef47211519b21b020131c0434003)
- Execute the following command 
```
go tool vet <PATH TO GO REPO>/src/cmd/vet/testdata 2>&1 | sed 's/[^:]*:[0-9]*:/main.go:1:/ > ./resources/externalreport/all-govet-report.txt'
```