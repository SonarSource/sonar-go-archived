package SwitchCaseTooBigCheck

func main() {
	alpha(1)
}

func alpha(a int) {
	switch a {
	case 0: // Compliant
		bar() // +1
		bar() // +2
		bar() // +3
    case 1: // Noncompliant {{Reduce this switch case number of lines from 4 to at most 3, for example by extracting code into functions.}}
//  ^^^^
		bar() //+1
		bar() //+2
		bar() //+3
		bar() //+4

	}
}
