package test

import "fmt"

func example(condition1, condition2 bool) {
	if condition1 {
	} else if condition1 { // Noncompliant
	}

	if condition2 {
	} else if condition1 {
	} else if condition1 { // Noncompliant
	}

	if condition1 {
	} else if condition2 {
	} else if condition1 { // Noncompliant [[secondary=12]] {{This branch can not be reached because the condition duplicates a previous condition in the same sequence of "if/else if" statements}}
	}
}

func ifInitializer(x int) {
	if x = 3; x > 0 {

	} else if x = -1; x > 0 { // Compliant, initializer has to be considered as part of condition

	}
}
