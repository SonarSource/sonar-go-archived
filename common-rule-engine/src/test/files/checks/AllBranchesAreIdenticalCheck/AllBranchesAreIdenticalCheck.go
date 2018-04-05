package NoIdenticalBranches

func foo(x int) int {
    if x == 1 {  // Noncompliant
//  ^^
        return 1
    } else {
        return 1
    }
}

func foo2(x int) int {
    if x == 1 {
        return 1
    } else {
        return 2
    }
}

func foo3(x int) int {
    if x == 1 {  // Compliant - RSPEC exception
        return 1
    } else if x == 2 {
        return 1
    }

    if x == 1 {
        return 1
    } else if x == 2 {
        return 2
    }

    if x == 1 {  // Noncompliant
        return 1
    } else if x == 2 {
        return 1
    } else {
        return 1
    }

    if x == 1 {
        return 1
    } else if x == 2 {
        return 1
    } else {
        return 2
    }

    if x == 1 {  // Compliant
		return 0
	} else if x == 2 {
		return 2
	} else if x == 3 {
		return 1
	} else {
		return 1
	}
}

func bar(x int) int {
    switch x {  // Noncompliant
    case 1:
        return 1
	case 2:
		return 1
    default:
        return 1
    }

	switch x {  // Compliant - doesn't have default
	case 1:
		return 1
	case 2:
		return 1
	}

	switch x {  // Compliant
	case 1:
		return 1
	case 2:
		return 2
	default:
		return 1
	}

	switch x {  // Compliant
	case 1:
		return 1
	default:
		return 2
	}

	switch x { // Compliant
	case 1, 2:
		return 0
	}
}
