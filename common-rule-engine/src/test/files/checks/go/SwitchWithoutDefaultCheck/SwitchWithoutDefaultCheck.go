package main

type f struct {
	a, b, c int
}

func foo(tag int, x f) {
	switch tag {
	default: // Compliant
		bar()
	case 0, 1, 2, 3:
		bar()
	case 4, 5, 6, 7:
		bar()
	}


	switch tag {
	case 0, 1, 2, 3:
		bar()
	default:  // Noncompliant {{Move this default to the start or end of the switch.}}
		bar()
	case 4, 5, 6, 7:
		bar()
	}

	switch tag { // Noncompliant {{Add a default case to this switch.}}
	case 0, 1, 2, 3:
		bar()
	case 4, 5, 6, 7:
		bar()
	}

	switch tag { // Compliant
	case 0, 1, 2, 3:
		bar()
	case 4, 5, 6, 7:
		bar()
	default:
		bar()
	}

	switch tag { // Compliant
	case 0, 1, 2, 3:
		bar()
	case 4, 5, 6, 7:
		switch tag {  // Noncompliant {{Add a default case to this switch.}}
		case 5:
			bar()
		case 6:
			bar()
		}
	default:
		bar()
	}

	switch x.(type) { // Noncompliant {{Add a default case to this switch.}}
	case nil:
		bar("x is nil")
	case bool, string:
		bar("type is bool or string")
	}

	switch x.(type) {
	case nil:
		bar("x is nil")
	default:  // Noncompliant {{Move this default to the start or end of the switch.}}
		bar("don't know the type")
	case bool, string:
		bar("type is bool or string")
	}

	switch x.(type) {
	default:  // Compliant
		bar("don't know the type")
	case nil:
		bar("x is nil")
	case bool, string:
		bar("type is bool or string")
	}

	switch x.(type) { // Compliant
	case nil:
		bar("x is nil")
	case bool, string:
		bar("type is bool or string")
	default:
		bar("don't know the type")
	}
}

func bar(s ... string) {
}