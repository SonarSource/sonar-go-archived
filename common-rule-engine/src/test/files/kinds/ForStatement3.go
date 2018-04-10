package main

func foo() {
  for i := 0;   // Noncompliant {{FOR_INIT}}
//    ^^^^^^
      i < 10;   // Noncompliant {{CONDITION}}
//    ^^^^^^
      i++     { // Noncompliant {{FOR_UPDATE}}
//    ^^^
  }
}
