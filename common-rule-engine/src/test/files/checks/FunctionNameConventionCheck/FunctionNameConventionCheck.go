package FunctionName

func Do_Something() { // Noncompliant {{Rename 'Do_Something' to match the regular expression ^[a-zA-Z0-9]+$.}}
}

func DoSomething() {
}

func doSomething() {
}

type MyStruct struct {
	X, Y int
}

func (v MyStruct) Method1() int {
	return v.X + v.Y
}

func (v MyStruct) method2() int {
	return v.X + v.Y
}

func (v MyStruct) method_3() int { // Noncompliant
	return v.X + v.Y
}
