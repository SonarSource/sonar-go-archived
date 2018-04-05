package main

import "fmt"

func fun1() {
//   ^^^^> {{Original implementation}}
	a, b := 1, 2
	b, a = a, b
}

func fun2() {  // Noncompliant {{Function is identical with function on line 5.}}
//   ^^^^
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

type type1 int
type type2 int

func (rcv type1) receiver1() {
	fmt.Println("hello")
	fmt.Println("hello")
}

func (rcv type2) receiver2() { // Compliant - different receiver type
	fmt.Println("hello")
	fmt.Println("hello")
}


func fooInt() int  {
	fmt.Println("hello")
	fmt.Println("hello")
	return 0
}

func fooFloat() float64 {   // Compliant - different return type
	fmt.Println("hello")
	fmt.Println("hello")
	return 0
}