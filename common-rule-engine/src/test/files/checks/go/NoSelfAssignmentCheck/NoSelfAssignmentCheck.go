package main

func noSelfAssignmentSingle() {
	a := 1
	a = a  // Noncompliant {{Remove this self assignment}}
//	^^^^^
}

func noSelfAssignmentMulti() {
	a, b := 1, 2  // Compliant
	b, a = b, a   // Noncompliant
}

func noSelfAssignmentSingleNoIssue() {
	a := 1     // Compliant
	a = a + 1  // Compliant
}

func noSelfAssignmentMultiNoIssue() {
	a, b := 1, 2
	a, b = b, a  // Compliant
}

func noSelfAssignmentPartialNoIssue() {
	a, b := 1, 2
	a, b = a, 5  // Compliant
	a = b        // Compliant
}

func declarationIsNotSelfAssignment(x int) {
	for i := 1; i < 10; i++  {
		x := x  // Compliant, x is fresh local var shadowing the argument
	}
}

func compoundAssignment(x int)  {
	x *= x  // Compliant
}
