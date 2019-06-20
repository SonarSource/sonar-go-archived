// SonarQube Go Plugin
// Copyright (C) 2018-2019 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

// The following directive is necessary to make the package coherent:

// This program generates 'goparser_generated.go'. It can be invoked by running "go generate"

package main

import (
	"encoding/json"
	"github.com/stretchr/testify/assert"
	"go/ast"
	"go/token"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

func slangFromString(source string, nodeQueryPath string) (*Node, []*Node, []*Token) {
	fileSet, astNode := astFromString(source)
	return toSlangTree(fileSet, astNode, source)
}

func astFromString(source string) (fileSet *token.FileSet, astFile *ast.File) {
	fileSet, astFile, err := readAstString("main.go", source)
	if err != nil {
		panic(err)
	}
	return
}

//Update all .txt files in resources/ast from all .go.source files
//Add "Test_" before to run in IDE
func fix_all_go_files_test_automatically(t *testing.T) {
	for _, file := range getAllGoFiles("resources/ast") {
		source, err := ioutil.ReadFile(file)
		if err != nil {
			panic(err)
		}

		actual := toJsonSlang(slangFromString(string(source), ""))
		d1 := []byte(actual)
		errWrite := ioutil.WriteFile(strings.Replace(file, "go.source", "json", 1), d1, 0644)
		if errWrite != nil {
			panic(errWrite)
		}
	}
}

func Test_all_go_files(t *testing.T) {
	for _, file := range getAllGoFiles("resources/ast") {
		source, err := ioutil.ReadFile(file)
		if err != nil {
			panic(err)
		}
		actual := toJsonSlang(slangFromString(string(source), ""))

		var jsonActual interface{}
		err1 := json.Unmarshal([]byte(actual), &jsonActual)
		if err1 != nil {
			panic(err1)
		}

		expectedData, err := ioutil.ReadFile(strings.Replace(file, "go.source", "json", 1))
		if err != nil {
			panic(err)
		}
		var jsonExpected map[string]interface{}

		err2 := json.Unmarshal(expectedData, &jsonExpected)
		if err2 != nil {
			panic(err2)
		}

		if !assert.Equal(t, jsonExpected, jsonActual) {
			t.Fatalf("got: %#v\nexpected: %#v", jsonActual, jsonExpected)
		}
	}
}

func getAllGoFiles(folder string) []string {
	var files []string

	err := filepath.Walk(folder, func(path string, info os.FileInfo, err error) error {
		if strings.HasSuffix(path, ".go.source") {
			files = append(files, path)
		}
		return nil
	})
	if err != nil {
		panic(err)
	}
	return files
}
