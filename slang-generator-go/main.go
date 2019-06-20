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

package main

import (
	"flag"
	"fmt"
	"os"
)

func exit() {
	flag.Usage()
	os.Exit(1)
}

type Params struct {
	dumpAst bool
	path    string
}

func parseArgs() Params {
	flag.Usage = func() {
		fmt.Printf("Usage: %s [options] source.go\n\n", os.Args[0])
		flag.PrintDefaults()
	}

	dumpAstFlag := flag.Bool("d", false, "dump ast (instead of JSON)")
	flag.Parse()
	var path string
	if len(flag.Args()) == 1 {
		path = flag.Args()[0]
	}

	return Params{
		dumpAst: *dumpAstFlag,
		path:    path,
	}
}

func main() {
	params := parseArgs()

	//Produce go AST
	fileSet, astFile, fileContent, err := readAstFile(params.path)
	if err != nil {
		panic(err)
	}

	if params.dumpAst {
		fmt.Println(render(astFile))
	} else {
		slangTree, comments, tokens := toSlangTree(fileSet, astFile, fileContent)
		fmt.Println(toJsonSlang(slangTree, comments, tokens))
	}
}
