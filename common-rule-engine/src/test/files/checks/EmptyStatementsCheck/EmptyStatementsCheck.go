package EmptyStatementsCheck

import "fmt"

func foo(i int) {

	i = 33;;	// Noncompliant {{Remove this empty statement.}}
	for i=0;i<10;i++{
		if i == 2 {
			fmt.Println(i);
		}
	}
}

func foo2(i int) {
	; // Noncompliant {{Remove this empty statement.}}
	for ; i < 10; i++ {
		; // Noncompliant {{Remove this empty statement.}}
	}
	for ; i < 10; i++ {
		; // Noncompliant {{Remove this empty statement.}}
		i+=2;
		break;
	}
}

func doSomethingElse() {
	fmt.Println("doSomethingElse");;    // Noncompliant {{Remove this empty statement.}}
}

func doSomethingElse1()  {
	fmt.Println("doSomethingElse")
}

func doSomethingElse2()  {
}