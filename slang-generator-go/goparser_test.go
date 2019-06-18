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
	"fmt"
	"go/ast"
	"go/token"
	"io/ioutil"
	"os"
	"path/filepath"
	"reflect"
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

func Test_fix_all_go_files_test_automatically(t *testing.T) {
	for _, file := range getAllGoFiles("resources/ast") {
		source, err := ioutil.ReadFile(file)
		if err != nil {
			panic(err)
		}

		actual := toJsonSlang(slangFromString(string(source), ""))
		d1 := []byte(actual)
		errWrite := ioutil.WriteFile(strings.Replace(file, "go", "txt", 1), d1, 0644)
		if errWrite != nil {
			panic(errWrite)
		}
	}
}


func Test_mapFile(t *testing.T) {
	for _, file := range getAllGoFiles("resources/ast") {
		source, err := ioutil.ReadFile(file)
		if err != nil {
			panic(err)
		}

		actual := toJsonSlang(slangFromString(string(source), ""))

		dat, err := ioutil.ReadFile(strings.Replace(file, "go", "txt", 1))
		if err != nil {
			panic(err)
		}

		expected := string(dat)

		if !reflect.DeepEqual(expected, actual) {
			t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
		}

		fmt.Println(file)
	}
}


func getAllGoFiles(folder string) []string {
	var files []string

	err := filepath.Walk(folder, func(path string, info os.FileInfo, err error) error {
		if strings.HasSuffix(path, ".go") {
			files = append(files, path)
		}
		return nil
	})
	if err != nil {
		panic(err)
	}
	return files
}


