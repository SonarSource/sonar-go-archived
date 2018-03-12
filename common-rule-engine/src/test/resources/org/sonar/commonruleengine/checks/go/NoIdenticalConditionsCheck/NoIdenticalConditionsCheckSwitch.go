package test

import "fmt"

func FooSwitch(param int) {
	switch true {
	case param > 1:
		fmt.Println(">1")
	case param < 1:
		fmt.Println("<1")
	case param > 1: // Noncompliant
		fmt.Println("impossible")
	}
}

func SwitchWithMultipleConditions(param int) {
	switch param {
	case 1, 2, 3:
		fmt.Println(">1")
	case 3, 4, 5: // Noncompliant
		fmt.Println("<1")
	}
}
