package main

func foo(a *Bar, i int) int {
  return a.field[i + 1] // Noncompliant {{ARRAY_KEY_EXPRESSION}}
//               ^^^^^
}
