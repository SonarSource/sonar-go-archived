package main

func nok() {
  for i := 1; i <= 10; j++ { // Noncompliant {{This loop's stop condition tests variable which is not incremented in update clause.}}
//^^^
    // ...
  }

  for i := 1; foo(something) > somethingElse; i++ { // Noncompliant
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

  for cond {
  }

  for {
  }
}
