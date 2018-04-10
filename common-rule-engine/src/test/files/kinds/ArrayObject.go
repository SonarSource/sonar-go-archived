package main

func foo(a *Bar, i int) int {
  var x [4]int
  return a.field[i + 1] // Noncompliant {{ARRAY_OBJECT_EXPRESSION}}
//       ^^^^^^^
}
