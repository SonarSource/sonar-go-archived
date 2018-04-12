package main

func foo() {
   bar(1)
   a.bar(1) // Noncompliant {{MEMBER_SELECT}}
// ^^^^^
   b := a.size // Noncompliant {{MEMBER_SELECT}}
//      ^^^^^^
   c := a.arr[3] // Noncompliant {{MEMBER_SELECT}}
//      ^^^^^
}
