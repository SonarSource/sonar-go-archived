package main

func noSelfAssignmentSingle() {
	a := 1
	a = a  // Noncompliant {{Remove or correct this useless self-assignment.}}
}

func noSelfAssignmentMulti() {
	a, b := 1, 2  // Compliant
	b, a = b, a   // Noncompliant {{Remove or correct this useless self-assignment.}}
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
