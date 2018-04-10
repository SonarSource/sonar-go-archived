package main

import "file.go" // Noncompliant {{LITERAL,STRING_LITERAL}}
//     ^^^^^^^^^

const N = 'a' // Noncompliant {{LITERAL,CHAR_LITERAL}}
//        ^^^

func foo() {
  bar(42) // Noncompliant {{LITERAL,INT_LITERAL,DECIMAL_LITERAL}}
//    ^^

  a := 10 // Noncompliant {{LITERAL,INT_LITERAL,DECIMAL_LITERAL}}
//     ^^
  b := 010 // Noncompliant {{LITERAL,INT_LITERAL,OCTAL_LITERAL}}
//     ^^^
  c := 0x10 // Noncompliant {{LITERAL,INT_LITERAL,HEX_LITERAL}}
//     ^^^^
  d := 1.0 // Noncompliant {{LITERAL,FLOAT_LITERAL}}
//     ^^^
  e := "10" // Noncompliant {{LITERAL,STRING_LITERAL}}
//     ^^^^
  f := '1' // Noncompliant {{LITERAL,CHAR_LITERAL}}
//     ^^^
  g := true // Noncompliant {{LITERAL,BOOLEAN_LITERAL}}
//     ^^^^
  bar(nil) // Noncompliant {{LITERAL,NULL_LITERAL}}
//    ^^^
}
