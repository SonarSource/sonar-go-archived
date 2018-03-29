package FunctionCognitiveComplexityCheck

func test1(a bool) int {
  return a
}

func test2(a bool) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 1 to the 0 allowed.}} [[effortToFix=1]]
//   ^^^^^
  if a {
//^^< {{+1}}
  }
}

func test3(a bool) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 7 to the 0 allowed.}} [[effortToFix=7]]
//   ^^^^^
  if a {
//^^< {{+1}}
    if a {
//  ^^< {{+2 (incl 1 for nesting)}}
      switch a {
//    ^^^^^^< {{+3 (incl 2 for nesting)}}
        case true:
        case false:
      }
    }
  }
  if a {
//^^< {{+1}}
  }
}

func extraConditions() bool { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}}
  return a && b || foo(b && c)
}

func extraConditions2() bool { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
  return a && (b || c) || d
}

func extraConditions3() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}}
  if (a && b || c || d) {}
}

func extraConditions4() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 5 to the 0 allowed.}}
    if (a && b || c && d || e) {}
}

func extraConditions5() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 5 to the 0 allowed.}}
  if (a || b && c || d && e) {}
}

func extraConditions6() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}}
  if (a && b && c || d || e) {}
}

func extraConditions7() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 1 to the 0 allowed.}}
  if (a) {}
}

func extraConditions8() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
  if (a && b && c && d && e) {}
}

func extraConditions9() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
  if (a || b || c || d || e) {}
}

func extraCondition10() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 4 to the 0 allowed.}}
  if (a && b && c || d || e && f){}
}

func switch2(){ // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 12 to the 0 allowed.}}
//   ^^^^^^^
    switch(foo){
//  ^^^^^^< {{+1}}
      case 1:
        break
      case ASSIGNMENT:
        if (lhs.is(Tree.Kind.IDENTIFIER)) {
//      ^^< {{+2 (incl 1 for nesting)}}
          if (                          a && b &&  c || d) {
//        ^^< {{+3 (incl 2 for nesting)}} ^^< {{+1}} ^^< {{+1}}
          }
          if(element.is(Tree.Kind.ASSIGNMENT)) {
//        ^^< {{+3 (incl 2 for nesting)}}
            out.remove(symbol)
          } else {
//          ^^^^< {{+1}}
            out.add(symbol)
          }
        }
        break
    }
  }

func extraCondition11() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
//   ^^^^^^^^^^^^^^^^
    if (a      || (b || c)) {}
//  ^^< {{+1}} ^^< {{+1}}
  }

func extraConditions12() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 7 to the 0 allowed.}}
//   ^^^^^^^^^^^^^^^^^
    if (
//  ^^< {{+1}}
      a && b   && c || d || e  && f && g  || (h || (i && j       || k)) || l || m){}
//      ^^< {{+1}}  ^^< {{+1}} ^^< {{+1}} ^^< {{+1}}  ^^< {{+1}} ^^< {{+1}}
  }

func breakWithLabel() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 1 to the 0 allowed.}}
//   ^^^^^^^^^^^^^^
  doABarrelRoll:
    for o := range objects {
//  ^^^< {{+1}}
      break doABarrelRoll; // TODO support +1 for break to a label
    }
}

func getValueToEval() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 6 to the 0 allowed.}}
//   ^^^^^^^^^^^^^^
    if (f1()    && foo == YELLOW) {
//  ^^< {{+1}}  ^^< {{+1}}

    } else if (f2()) {
//    ^^^^< {{+1}}
    } else {
//    ^^^^< {{+1}}
      for {
//    ^^^< {{+2 (incl 1 for nesting)}}
        doTheThing()
      }
      panic("illegal state")
    }
}

func extraConditions() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 10 to the 0 allowed.}}
//   ^^^^^^^^^^^^^^^
    if (a < b) {
//  ^^< {{+1}}
      doTheThing()
    }

    if (a == b || c > 3 || b-7 == c) {
//  ^^< {{+1}} ^^< {{+1}}
      for ; a > 0                      && b < 10; a++ {
//    ^^^< {{+2 (incl 1 for nesting)}} ^^< {{+1}}
        doTheOtherThing()
      }
    }

    for ; a > 0 || b != YELLOW ; {
//  ^^^< {{+1}} ^^< {{+1}}
    }

    for i := 0; i < 10 && j > 20; i++ {
//  ^^^< {{+1}}        ^^< {{+1}}
      doSomethingElse()
    }
}

func main2() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
//   ^^^^^
    r := func() {
      if (condition) {
//    ^^< {{+2 (incl 1 for nesting)}}
      }
    }
}
