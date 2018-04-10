package main

func foo(a *Bar, i int) int {
  return a.field[i + 1] // Noncompliant {{ARRAY_ACCESS_EXPRESSION}}
//       ^^^^^^^^^^^^^^
}
