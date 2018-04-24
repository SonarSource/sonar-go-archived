package StringLiteralDuplicatedCheck

func duplication2(s string) {
	duplication("This literal is duplicated")  // Compliant
	duplication("This literal is duplicated")
	duplication("This literal is duplicated")
	duplication("This literal is duplicated")
}

func duplication3(s string) {
	duplication("Another literal")  // Noncompliant
	duplication("Another literal")
	duplication("Another literal")
	duplication("Another literal")
	duplication("Another literal")
}
