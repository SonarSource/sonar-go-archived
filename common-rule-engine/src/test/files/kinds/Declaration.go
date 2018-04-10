package main

   import "file.go" // Noncompliant {{IMPORT}}
// ^^^^^^^^^^^^^^^^

func foo() {
  var a int // Noncompliant {{VARIABLE_DECLARATION}}
  //  ^^^^^

  const A = 1 // Noncompliant {{CONSTANT_DECLARATION}}
  //    ^^^^^
}
