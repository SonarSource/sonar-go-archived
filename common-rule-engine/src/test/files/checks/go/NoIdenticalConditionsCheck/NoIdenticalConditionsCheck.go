package test

func example(condition1, condition2 bool) {
	if condition1 {
	} else if condition1 { // Noncompliant
	//        ^^^^^^^^^^
	}

	if condition2 {
	} else if condition1 {
	} else if condition1 { // Noncompliant
	}

	if condition1 {
	} else if condition2 {
	} else if condition1 { // Noncompliant {{This condition is same as one already tested on line 14.}}
	//        ^^^^^^^^^^
	}
}

func ifInitializer(x int) {
	if x = 3; x > 0 {

	} else if x = -1; x > 0 { // Compliant, initializer has to be considered as part of condition

	}
}
