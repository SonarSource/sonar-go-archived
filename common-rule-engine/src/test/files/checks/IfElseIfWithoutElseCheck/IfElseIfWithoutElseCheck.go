package IfElseIfWithoutElseCheck

func foo(x int) {
  if x == 0 { // Noncompliant {{Add the missing else clause.}}
//^^
	  doSomething()
  } else if x == 1 {
	  doSomethingElse()
  }
  if x == 0 {
	  doSomething()
  } else if x == 1 {
	  doSomethingElse()
  } else {
	  return errors.New("unsupported int")
  }
  if x == 0 {
    doSomething()
  }

   if x == 0 { // Noncompliant
  	  doSomething()
    } else if x == 1 {
  	  doSomethingElse()
    } else if x == 2 {
      doSomethingElse()
    } else if x == 3 {
      doSomethingElse()
    }
}
