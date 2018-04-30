package main


func foo() {
  if len(arr) >= 0 {} // Noncompliant {{The length of a collection is always ">=0", so update this test to either "==0" or ">0".}}
//   ^^^^^^^^^^^^^
  if len(arr) < 0 {}  // Noncompliant

  if foo.len(arr) < 0 {}
  if len.foo(arr) < 0 {}
  if len < 0 {}
  if len(arr) < 5 {}
  if len(arr) > 0 {}

}
