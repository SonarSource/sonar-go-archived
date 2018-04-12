package main

func foo() {
   method(1, // Noncompliant {{ARGUMENT}}
//        ^
          bar(), // Noncompliant {{ARGUMENT}}
//        ^^^^^
          2 + 2) // Noncompliant {{ARGUMENT}}
//        ^^^^^
}
