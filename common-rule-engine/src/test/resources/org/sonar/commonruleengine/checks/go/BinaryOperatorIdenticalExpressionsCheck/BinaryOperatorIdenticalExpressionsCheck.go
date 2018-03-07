package main

func main() {
	v1 := true || true // Noncompliant
	v2 := (true && false) && (true && false) // Noncompliant

	v3 := 1 == 1 // Noncompliant
	v4 := 1 != 2

	v5 := foo() + foo() // Noncompliant
	v6 := foo() - bar() // Compliant
}

func foo() {
	return 1;
}

func bar() {
	return 1;
}
