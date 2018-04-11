package main
func foo(tag int) {
   switch tag { // Noncompliant {{Reduce the number of switch cases from 31 to at most 5.}}
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

func foo2(tag int) {
  switch tag { // Noncompliant
//^^^^^^
  default:
//^^^^^^^< {{+1}}
		bar()
  case 0, 1, 2, 3:
//^^^^< {{+1}}
	bar()
  case 4, 5, 6, 7:
//^^^^< {{+1}}
		bar()
  case 4, 5, 6, 7:
//^^^^< {{+1}}
    bar()
  case 4, 5, 6, 7:
//^^^^< {{+1}}
    bar()
  case 0, 1, 2, 3:
//^^^^< {{+1}}
    bar()
  case 4, 5, 6, 7:
//^^^^< {{+1}}
    bar()
  }
}
func bar(s ... string) {
}
