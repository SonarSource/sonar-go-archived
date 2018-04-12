package main

func foo() {
  var a int // Noncompliant {{TYPE}}
  //    ^^^
  var c, d int = 1, 2 // Noncompliant {{TYPE}}
  //       ^^^

  var e = 5

  var (
      c, d int // Noncompliant {{TYPE}}
  //       ^^^
      e, f string // Noncompliant {{TYPE}}
  //       ^^^^^^
      g = 7
  )

}

func (t *Type) foo2() {} // Noncompliant {{TYPE}}
//      ^^^^^

func foo2(x,
          y int) {} // Noncompliant {{TYPE}}
//          ^^^

func foo3() (out1,
             out2 int) {} // Noncompliant {{TYPE}}
//                ^^^
