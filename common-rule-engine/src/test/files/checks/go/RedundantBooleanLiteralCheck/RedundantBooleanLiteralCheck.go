package RedundantBooleanLiteralCheck


func test(flag bool) {
	if flag == true { // Noncompliant {{Remove this redundant boolean literal}}
	//         ^^^^

	}
	test := flag || true // Noncompliant
	test = false && flag // Noncompliant
	if false == test { // Noncompliant

	}
}

