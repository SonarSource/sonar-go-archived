package FunctionTooBigCheckMax5

func foo1() {
}
type AI interface {
    //define all methods that you want to override
    method2()
}

func foo2() { // Noncompliant {{This function has 6 lines of code, which is greater than the 5 authorized. Split it into smaller functions.}}
//   ^^^^
  foo1()
  foo1()
  foo1()
  foo1()
}

