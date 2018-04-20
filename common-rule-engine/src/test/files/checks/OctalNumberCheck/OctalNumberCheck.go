package Foo

var a = 0
var b = 1
var c = 42
var mode = 0644 // 4-digit octal numbers are ignored as are often used for file permissions
var d = 010 // Noncompliant {{Use decimal rather than octal values.}}
//      ^^^
var e = 00 // Noncompliant
var f = 0.
var g = 0x00
var h = 0X00

func foo() {
  foo(010)   // Noncompliant
}

func ignoreBitwiseOperations() {
  var a = 0
  a &= 02
  a |= 02
  a = a & 02
  a = a &^ 02
  a = a | 02
  a = a << 02
  a = a >> 02
}
