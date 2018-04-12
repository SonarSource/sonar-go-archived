package main

func foo() int {
  switch a {
    case 0, 1: return 1 // Noncompliant {{CASE}}
//  ^^^^^^^^^^^^^^^^^^^
    case 2: return 2 // Noncompliant {{CASE}}
//  ^^^^^^^^^^^^^^^^
    default: return 3 // Noncompliant {{CASE,DEFAULT_CASE}}
//  ^^^^^^^^^^^^^^^^^
  }
}
