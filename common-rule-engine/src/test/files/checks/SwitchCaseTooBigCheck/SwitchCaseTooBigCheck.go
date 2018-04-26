package SwitchCaseTooBigCheck

import (
	"fmt"
	"net"
)

type A struct{
	a string
}

func main(){
	foo(1)
}

func foo(a int){
	switch a{
		case 1:		// compliant
			fmt.Println("OS X1.")
			fmt.Println("OS X2.")
			fmt.Println("OS X3.")



        case 2:	 // Noncompliant {{Reduce this switch case number of lines from 10 to at most 5, for example by extracting code into methods.}}
//      ^^^^
			fmt.Println("Linux.")
			defer bar()
			go bar()
			go func() {
				//test
			}()
			a := A{"2"}
			a = A{"1"}
			fmt.Println(a)
		case 5:    // Noncompliant
			for i := range []int{2, 3, 4}{
				idxPath := i
				if idxPath > 0 {
					if isIP := net.ParseIP("192.0.0.1") != nil;
					!isIP {
						idxPath = 0
					}
				}
				if len("0123456789") > 0 {
					if isIP := net.ParseIP("localhost") != nil;
					isIP {
						idxPath = 0
					}
				}
			}
		case 6:	// Noncompliant
			if a == 1{
				fmt.Println(a)
				fmt.Println(a)
				fmt.Println(a)
			}else{
			fmt.Println(a)
		}
		case 3:
		case 4:
		default:  // Noncompliant
			// freebsd, openbsd,
			// plan9, windows...
			a := A{"test value"}
			fmt.Println(a)
			bar()
			go func() {//test
			}()
			break
	}
}

func bar(){
	// test
}