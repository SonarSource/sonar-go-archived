package NoHardcodedCredentialsCheck

func test()  {
	   pwd := "supersecret" // Noncompliant
	// ^^^
	   myPassword := "supersecret" // Noncompliant
	// ^^^^^^^^^^
	myPaSsWord := "supersecret" // Noncompliant
	myPassword := "" // Empty is OK

	url := "login=user&passwd=secret" // Noncompliant
	url = "login=user&passwd=" // This is OK
}

func explicitDecl()  {
	var pwd string
	pwd = "secret"  // Noncompliant
	pwd = nil
}
