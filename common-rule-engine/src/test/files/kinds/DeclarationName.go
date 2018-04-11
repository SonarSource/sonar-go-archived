package main

func foo() {
  var a int // Noncompliant {{VARIABLE_NAME}}
  //  ^
  var c, // Noncompliant {{VARIABLE_NAME}}
  //  ^
      d int = 1, 2 // Noncompliant {{VARIABLE_NAME}}
  //  ^

  const A = 1 // Noncompliant {{VARIABLE_NAME}}
  //    ^
}
