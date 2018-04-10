package main

func foo() {
  for i := 0; i < 10; i++ { // Noncompliant {{FOR_KEYWORD}}
//^^^
  }
}
