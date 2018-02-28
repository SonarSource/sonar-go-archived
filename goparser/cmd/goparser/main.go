// Command line interface to goparser
package main

import (
	"flag"
	"fmt"
	"os"
	"github.com/SonarSource/sonar-go/goparser/test-go/render"
	"github.com/SonarSource/sonar-go/goparser"
)

func exit() {
	flag.Usage()
	os.Exit(1)
}

func parseArgs() string {
	flag.Usage = func() {
		fmt.Printf("Usage: %s [options] source.go\n\n", os.Args[0])
		flag.PrintDefaults()
	}

	flag.Parse()

	if len(flag.Args()) != 1 {
		exit()
	}

	return flag.Args()[0]
}


func main() {
	filename := parseArgs()

	astFile := goparser.ReadAstFile(filename)
	_ = render.Render(astFile)
	fmt.Println(render.Render(astFile))

	//uast := mapFile(astFile)
	//printJson(uast)
}
