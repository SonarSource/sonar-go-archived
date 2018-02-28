// Command line interface to goparser
package main

import (
	"flag"
	"fmt"
	"os"
	"github.com/SonarSource/sonar-go/goparser/utils"
	"github.com/SonarSource/sonar-go/goparser"
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

	if len(flag.Args()) != 1 {
		exit()
	}

	return Params{
		dumpAst: *dumpAstFlag,
		path:    flag.Args()[0],
	}
}

func main() {
	params := parseArgs()

	astFile := goparser.ReadAstFile(params.path)

	if params.dumpAst {
		fmt.Println(render.Render(astFile))
	} else {
		uast := goparser.MapFile(astFile)
		goparser.PrintJson(uast)
	}
}
