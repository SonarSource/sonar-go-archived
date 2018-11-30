package UnreachableCodeCheck

import "fmt"

func add(x, y int) int {
	return x + y // Noncompliant {{Refactor this piece of code to not have any dead code after this return.}}
	z := x + y
	fmt.Print(z)
	return 0
}

func loop() {
	for i := 1; i < 10; i++ {
		fmt.Print(i)
		break // Noncompliant {{Refactor this piece of code to not have any dead code after this break.}}
		fmt.Print(i)
	}
}

func mandatory_return() int {
	panic("No namespace syscall support") // Noncompliant {{Refactor this piece of code to not have any dead code after this panic.}}
	return 0                              // this was necessary in some old go version, but it's not the case anymore
}

func ignore_labels() {
	return
label:
	fmt.Print("labeled statement")
	fmt.Print("labeled statement")
}

func switchcase(x int) {
	switch x {
	case 1:
		return // Noncompliant
		fmt.Print("Hello")
	}
}

func multipleJumps() {
	goto lable
lable:
	fmt.Println("happen")
	return // Noncompliant {{Refactor this piece of code to not have any dead code after this return.}}
	fmt.Println("never happen")
}
