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
