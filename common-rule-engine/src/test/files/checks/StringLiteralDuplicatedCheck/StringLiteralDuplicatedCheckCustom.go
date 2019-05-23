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

type duplication4 struct {
	A int `json:"string"`  // Compliant
	B int `json:"string"`
	C int `json:"string"`
	D int `json:"string"`
	E int `json:"string"`
}
