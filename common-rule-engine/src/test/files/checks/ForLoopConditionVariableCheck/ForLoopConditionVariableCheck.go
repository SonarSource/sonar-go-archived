package main

func nok() {
  for i := 1; i <= 10; j++ { // Noncompliant {{This loop's stop condition tests variable which is not incremented in update clause.}}
//^^^
    // ...
  }

}

func ok() {

  for i := 1; i <= 10; i++ {
    // ...
  }

  for i := 1; i <= 10; foo(i) {
    // ...
  }

  for i := 1; i <= 10 && j >= 1; j++ {
    // ...
  }

  for i := 1; i <= 10; {
    // ...
  }

  // ok, more than 2 identifiers in condition
  for i := 1; foo(something) > somethingElse; i++ {
    // ...
  }

  // ok, no identifiers
  for i := 1; true; i++ {
    // ...
  }

  for cond {
  }

  for {
  }
}
