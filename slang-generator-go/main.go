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
	dumpAst      bool
	dumpKinds    bool
	path         string
	slangAstFlag bool
}

func parseArgs() Params {
	flag.Usage = func() {
		fmt.Printf("Usage: %s [options] source.go\n\n", os.Args[0])
		flag.PrintDefaults()
	}

	slangAstFlag := flag.Bool("slang", false, "return SLANG ast")
	dumpAstFlag := flag.Bool("d", false, "dump ast (instead of JSON)")
	dumpKinds := flag.Bool("k", false, "dump supported uast kinds")
	flag.Parse()
	var path string
	if len(flag.Args()) == 1 {
		path = flag.Args()[0]
	}
	if !*dumpKinds && len(path) == 0 {
		exit()
	}
	return Params{
		dumpAst:      *dumpAstFlag,
		dumpKinds:    *dumpKinds,
		path:         path,
		slangAstFlag: *slangAstFlag,
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
		if params.slangAstFlag {
			slangTree := toSlangTree(fileSet, astFile, fileContent)
			fmt.Println(toJsonSlang(slangTree))
		}
	}
}
