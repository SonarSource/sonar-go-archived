package Main

func func1(p int) {  // Noncompliant {{Remove the unused function parameter(s) "p".}}
//         ^
  foo("p")
}

func func2(p1, p2 int) {  // Noncompliant {{Remove the unused function parameter(s) "p1", "p2".}}
//         ^^  ^^<
}

func func2(p1 int, p2 int) {  // Noncompliant {{Remove the unused function parameter(s) "p1", "p2".}}
//         ^^      ^^<
}

func func3(p int) {
   foo(p)
}

func functionLiteral(p int) {
  foo(func(p int) { }, p) // Noncompliant
//         ^
}

func func1(_ int) {
  foo()
}

func (receiver *Point) method(p int) { // OK, methods are ignored
}

func foo(p int) { // FN

   bar := func(p int) { // Noncompliant
//             ^
   }

   bar(1)
}
