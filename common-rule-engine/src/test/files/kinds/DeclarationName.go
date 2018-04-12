package main

func foo1() {
  var a int // Noncompliant {{VARIABLE_NAME}}
  //  ^
  var b, // Noncompliant {{VARIABLE_NAME}}
  //  ^
      c int = 1, 2 // Noncompliant {{VARIABLE_NAME}}
  //  ^

  const A = 1 // Noncompliant {{VARIABLE_NAME}}
  //    ^
}

func (t *Type) foo2() {} // Noncompliant {{VARIABLE_NAME}}
//    ^

func foo2(x, // Noncompliant {{VARIABLE_NAME}}
//        ^
          y int) {} // Noncompliant {{VARIABLE_NAME}}
//        ^

func foo3() (out1, // Noncompliant {{VARIABLE_NAME}}
//           ^^^^
             out2 int) {} // Noncompliant {{VARIABLE_NAME}}
//           ^^^^
