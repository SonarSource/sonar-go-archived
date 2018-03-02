// S122
package samples

func foo() {

  foo() // Compliant

  foo(); bar() // Noncompliant {{Reformat the code to have only one statement per line.}}

  if (bar() == bar()) { // Compliant
  }

  f := func() { foo(); bar() } // Noncompliant
  f()

  if bar() == 0 { foo() } // Compliant
  if bar() == 0 { foo(); bar() } // Noncompliant
  if bar() == 0 { foo() } else { bar() } // Compliant
}

func bar() int {
  return 0
}
