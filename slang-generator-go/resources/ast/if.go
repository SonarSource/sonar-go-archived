package main

import "fmt"

func main() {
	if true {
		fmt.Println("thenPart")
	}

	if false {
		fmt.Println("thenPart")
	} else {
		fmt.Println("elsePart")
	}

	if (true) {
		fmt.Println("thenPart")

	}
}
