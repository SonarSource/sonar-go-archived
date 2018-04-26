package UselessIfCheck

func f(x bool) {
  if true {// Noncompliant {{Remove this useless "if" statement.}}
  ^^
  	doSomething()
  }

  if false { // Noncompliant {{Remove this useless "if" statement.}}
  	doSomething()
  } else if x {
  	doSomething()
  }

  if x {
    doSomething()
  }
}
