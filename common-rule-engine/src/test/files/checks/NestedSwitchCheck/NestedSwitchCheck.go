package main

func foo(x,y, z int) {
	switch x {
	case 0 :
		bar()
	case 1:
        switch y { // Noncompliant {{Refactor the code to eliminate this nested "switch".}}
    //  ^^^^^^
		case 0:
			bar()
		case 1:
			switch z { // Noncompliant
			case 0:
				bar()
			}
		}
	case 2:
		if y > 4 {
			switch y { // Noncompliant
			case 5:
				bar()
			case 6:
				bar()
			}
		}
	case 3:
		myFunc := func(myVar int) {
			switch myVar { // Compliant - within function literal
			case 0:
				bar()
			case 1:
				bar()
			}
		}
		myFunc(y)
	case 4:
		type MyLocalType interface {
			// can not define body of a func within an interface
			qix(i int)
		}
	}

	switch x {
	case 0:
		bar()
	case 1:
		bar()
	}
}

func bar() {}
