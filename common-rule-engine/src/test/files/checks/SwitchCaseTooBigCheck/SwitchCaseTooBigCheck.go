package SwitchCaseTooBigCheck

import (
	"fmt"
)

type A struct {
	a string
}

func main() {
	foo(1)
}

func foo(a int) {
	switch a {
	case 1: // Compliant
		fmt.Println("OS X1.")
		fmt.Println("OS X2.")
		fmt.Println("OS X3.")

    case 2: // Noncompliant {{Reduce this switch case number of lines from 7 to at most 5, for example by extracting code into functions.}}
//  ^^^^
		bar()
		bar()
		bar()
		bar()
		bar()
		bar()
		bar()
	case 6: // Noncompliant  {{Reduce this switch case number of lines from 7 to at most 5, for example by extracting code into functions.}}
		if a == 1 {
			fmt.Println(a)
			fmt.Println(a)
			fmt.Println(a)
		} else {
			fmt.Println(a)
		}
	case 7: // Compliant
		//...
		// ...
		//...
		//...
		//...
		//...
		fmt.Println(a)
	case 3:
	case 4:
	default: // Noncompliant
		// freebsd, openbsd,
		// plan9, windows...
		a := A{"test value"}
		fmt.Println(a)
		bar()
		go func() { //test
		}()
		break
	}
}

func bar() {
	// test
}
