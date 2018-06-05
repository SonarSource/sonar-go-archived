# External Linter Reports

`Go vet` and `Golint` reports don't have a key associated to each issue.
Within `sonar-go` we are generating a key based on, respectively, `go vet` and `golint` error messages.

In order to maintain the ("error message" - "key") matching, we generate a report from officially supported test data, i.e.:
- `<GOLANG REPO>/src/cmd/vet/testdata` for `go vet`
- `<GOLINT REPO>/testdata` for `golint`

## go vet

To generate file `all-govet-report`:

- Clone [golang repository locally](https://github.com/golang/go) (commit 3d6e4ec0a8c2ef47211519b21b020131c0434003)
- Execute the following command 
```
go tool vet <PATH TO GO REPO>/src/cmd/vet/testdata 2>&1 | sed 's/[^:]*:[0-9]*:/main.go:1:/ | sort --unique > ./resources/externalreport/all-govet-report.txt'
```

## golint

To generate file `all-golint-report`:

- Clone [golint repository locally](https://github.com/golang/lint) (commit 470b6b0bb3005eda157f0275e2e4895055396a81)

```
for X in <PATH TO GOLINT REPO>/testdata; do
    golint $X | sed s/[^:]*:[0-9]*:[0-9]*:/main.go:1:1:/ | sort --unique >> ./resources/externalreport/all-golint-report.txt
done
```

Note: In the reports, we are replacing origin file name with "main.go" to simplify report processing while unit testing.