package main

func main() {
	v1 := true || true // Noncompliant {{Correct one of the identical sub-expressions on both sides of operator "||".}}
	//    ^^^^>   ^^^^
	v2 := (true && false) && (true && false) // Noncompliant
	//    ^^^^^^^^^^^^^^^>   ^^^^^^^^^^^^^^^

	v3 := 1 == 1 // Noncompliant
	v4 := 1 != 2

	v5 := foo() / foo() // Noncompliant
	v6 := foo() - bar() // Compliant

	v7 := 1024 * 1024 // Compliant '*' ignored
	v8 := 1 + 1 // Compliant '+' ignored
	v9 := 1 << 1 // Compliant << ignored
}

func foo() {
	return 1;
}

func bar() {
	return 1;
}
