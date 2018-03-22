package main

import "fmt"

func fun1() {
//^[sc=1;el=+4;ec=1]>
	a, b := 1, 2
	b, a = a, b
}
// FIXME(issue #152) should only highlight function signature
func fun2() {  // Noncompliant {{Function is identical with function on line 5.}}
//^[sc=1;el=+4;ec=1]
	a, b := 1, 2
	b, a = a, b
}

func print1() {
    fmt.Println("hello")
    fmt.Println("hello")
}

func print2() {  // Noncompliant
    fmt.Println("hello")
    fmt.Println("hello")
}
