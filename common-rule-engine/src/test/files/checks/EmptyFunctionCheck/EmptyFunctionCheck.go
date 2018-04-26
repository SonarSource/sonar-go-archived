package EmptyMethodsCheck

import "fmt"

func bar(a, b int) string{
	return ""
}
// Noncompliant@+1 {{Add a nested comment explaining why this function is empty or complete the implementation.}}
func foo(){}

// Noncompliant@+1
func alpha(a string){
}

func min(x int, y int) int {
	if x < y {
		return x
	}
	return y
}

func main() {
	sum := func(a, b int) int { return a+b } (3, 4) // compliant
	fmt.Println(sum)

	// go routines should also be reported (UastNode.Kind is FUNCTION_LITERAL)
	// Noncompliant@+1
	go func() {
	}()

	a := 1
	// Compliant
	go func(){
		a++
	}()

	/* anonymous functions */
	f := func(x, y int) int{return x+y}  // Compliant
	f(1,2)

	// Noncompliant@+1
	f1 := func(x,y int) {}
	//    ^^^^^^^^^^^^^^^^
	f1(1,2)
}

type Point struct {
	x, y uint64
}

// Noncompliant@+1
func(p *Point) Cycle(factor uint64){
}

func(p *Point)Length(){
	// compliant
}

func (p *Point) Scale(factor uint64) {
	p.x *= factor
	p.y *= factor
	p.Length()
}

