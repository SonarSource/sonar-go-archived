package main
func foo() {
  var a int // Noncompliant {{VARIABLE_DECLARATION}}
  //  ^^^^^

  const A = 1 // Noncompliant {{CONSTANT_DECLARATION}}
  //    ^^^^^
}
