package main

import (
	"time"
	"fmt"
)

func foo(tag int, x interface{}) {
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
	default:  // Noncompliant {{Move this "default" case clause to the beginning or end of this "switch" statement.}}
		bar()
	case 4, 5, 6, 7:
		bar()
	}

	switch tag { // Compliant
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
		switch tag {  // Compliant
		case 5:
			bar()
		case 6:
			bar()
		}
	default:
		bar()
	}

	switch x.(type) { // Compliant
	case nil:
		bar("x is nil")
	case bool, string:
		bar("type is bool or string")
	}

	switch x.(type) {
	case nil:
		bar("x is nil")
	default:  // Noncompliant {{Move this "default" case clause to the beginning or end of this "switch" statement.}}
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

	c1 := make(chan string)
	c2 := make(chan string)

	go func() {
		time.Sleep(1 * time.Second)
		c1 <- "one"
	}()
	go func() {
		time.Sleep(2 * time.Second)
		c2 <- "two"
	}()

	switch tag { // Compliant
	case 0:
		bar("hello");
		break;
	case 1:
		select {
		case msg1 := <-c1:
			fmt.Println("received", msg1)
		default: // Compliant
			fmt.Println("should not happen")
		case msg2 := <-c2:
			fmt.Println("received", msg2)
		}
		break;
	case 2:
		bar("hello");
		break;
	}

}

func bar(s ... string) {
}
