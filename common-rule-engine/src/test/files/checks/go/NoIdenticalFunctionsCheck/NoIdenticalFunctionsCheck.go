package main

import "fmt"

func fun1() {
	a, b := 1, 2
	b, a = a, b
}

func fun2() {  // Noncompliant
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
