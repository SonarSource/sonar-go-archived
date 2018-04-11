package main

   import "file.go" // Noncompliant {{IMPORT}}
// ^^^^^^^^^^^^^^^^

func foo() {
  var a int // Noncompliant {{VARIABLE_DECLARATION}}
//    ^^^^^

  const A = 1 // Noncompliant {{VARIABLE_DECLARATION,CONSTANT_DECLARATION}}
//      ^^^^^

  var (
    c, d int // Noncompliant {{VARIABLE_DECLARATION}}
//  ^^^^^^^^
    e, f string // Noncompliant {{VARIABLE_DECLARATION}}
//  ^^^^^^^^^^^
  )
}
