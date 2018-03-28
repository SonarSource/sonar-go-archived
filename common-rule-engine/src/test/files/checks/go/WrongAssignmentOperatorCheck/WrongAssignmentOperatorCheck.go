package main

func foo() {
	var target = -5
	var num = 3
	var a, b, c = true, true, true

	target =-num // Noncompliant {{Was "-=" meant instead?}} [[sc=12;ec=14]]
	target = -num // Compliant intent to assign inverse value of num is clear
	target =num

	target += num;
	target =+ num; // Noncompliant {{Was "+=" meant instead?}} [[sc=12;ec=14]]
	target = +num;
	target=+num; // Compliant - no spaces between variable, operator and expression

	target ^= num;
	target =^ num; // Compliant - may be ambiguous for Go, but not targeted by the rule

	target, num =+ 0,1 // compliant

	a = b != c
	b =! c // Noncompliant {{Add a space between "=" and "!" to avoid confusion.}}

	bar0(a,b,c)
	bar1(target, num)
}

func bar0(b ... bool) {}
func bar1(i ... int) {}
