package main

type f struct {
	a, b, c int
}

func foo(tag int, x f) {
   switch tag { // Noncompliant {{Reduce the number of non-empty switch cases from 31 to at most 5.}}
// ^^^^^^
	default:
		bar()
	case 0, 1, 2, 3:
		bar()
	case 4, 5, 6, 7:
		bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
	}

}

func foo2(tag int, x f) {
  switch tag { // Noncompliant
//^^^^^^
	default:
		bar()
	case 0, 1, 2, 3:
		bar()
	case 4, 5, 6, 7:
		bar()
  case 4, 5, 6, 7:
    bar()
  case 4, 5, 6, 7:
    bar()
  case 0, 1, 2, 3:
    bar()
  case 4, 5, 6, 7:
    bar()
  }
}
func bar(s ... string) {
}
