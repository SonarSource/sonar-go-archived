   package main // Noncompliant {{PACKAGE}}
// ^^^^^^^^^^^^

   import "file.go" // Noncompliant {{IMPORT}}
// ^^^^^^^^^^^^^^^^

func foo() {
  var a int // Noncompliant {{VARIABLE_DECLARATION}}
//    ^^^^^

  const A = 1 // Noncompliant {{VARIABLE_DECLARATION,CONSTANT_DECLARATION}}
//      ^^^^^

  var (
    c, d int // Noncompliant {{VARIABLE_DECLARATION}}
//  ^^^^^^^^
    e, f string // Noncompliant {{VARIABLE_DECLARATION}}
//  ^^^^^^^^^^^
  )
}

func (t *Type) foo2() {} // Noncompliant {{VARIABLE_DECLARATION,PARAMETER}}
//    ^^^^^^^

func foo2(x, y int) {} // Noncompliant {{VARIABLE_DECLARATION,PARAMETER}}
//        ^^^^^^^^

func foo3() (out1, out2 int) {} // Noncompliant {{VARIABLE_DECLARATION}}
//           ^^^^^^^^^^^^^^
