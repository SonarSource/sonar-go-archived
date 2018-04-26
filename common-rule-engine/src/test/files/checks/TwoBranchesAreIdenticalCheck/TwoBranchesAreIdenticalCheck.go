package main

func switchStatement(){
  switch (1) {
    case 1:
//  ^[el=+3;ec=17] >
      foo("plop")
      foo("plop")
    case 2:
      foo("bar") //Compliant
    case 3:
    case 4: // Noncompliant {{This case's code block is the same as the block for the case on line 5.}}
//  ^[el=+3;ec=17]
      foo("plop")
      foo("plop")
    case 5: // Noncompliant {{This case's code block is the same as the block for the case on line 5.}}
      foo("plop")
      foo("plop")
  }

  switch (1) {
    case 1:
      f(1)
      f(2)
    case 2:
      f(3)
      f(4)
  }

  switch (1) {
    case 1:
      trivial()
    case 2:
      trivial()
  }

  switch (1) {
    case 1:
      trivial()
    case 2:
      trivial()
    case 3:
    default:
  }

  switch (1) {
    case 1:
      f()
      nonTrivial()
    case 2: // Noncompliant
      f()
      nonTrivial()
    case 3:
  }

  switch (1) {
    case 1:
      f(1)
      break
  }

  switch (1) {
    case 1:
      f(1)
      foo(1)
      break
    case 2: // Noncompliant
      f(1)
      foo(1)
      break
  }

  switch (1) {
    case 1:
      f(1)
      foo(1)
      break
    case 2: // Noncompliant
      f(1)
      foo(1)
      break
    case 3:
      break
  }
}

func ifStatement() {
  if (true) {
    foo("foo")
  } else if (true) {
    // skip empty blocks
  } else if (true) {
    // skip empty blocks
  } else if (true) {
    foo("bar")
  } else if (true) { // Compliant - trivial
    foo("foo")
  } else { // Compliant - trivial
    foo("foo")
  }

  if (true) {
    foo("foo")
    foo("foo")
  } else if (true) {
    // skip empty blocks
  } else if (true) {
    // skip empty blocks
  } else if (true) {
    foo("bar")
  } else if (true) { // Noncompliant {{This branch's code block is the same as the block for the branch on line 102.}}
    foo("foo")
    foo("foo")
  } else { // Noncompliant {{This branch's code block is the same as the block for the branch on line 102.}}
    foo("foo")
    foo("foo")
  }
  if (true) {
    1
  }

  if (true) {
    f()
  } else if (true) {
    f()
  } else {
    g()
  }


  if (true) {
    f()
    f()
  } else if (true) { // Noncompliant
    f()
    f()
  } else if (true) {
    g()
    g()
  } else if (true) {  // Noncompliant
    g()
    g()
  }

}

