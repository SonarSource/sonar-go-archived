package NoHardcodedCredentialsCheck

func test()  {
	pwd := "supersecret" // Noncompliant
	myPassword := "supersecret" // Noncompliant
	url := "login=user&passwd=secret" // Noncompliant
	url = "login=user&passwd=" // This is OK
}

func explicitDecl()  {
	var pwd string
	pwd = "secret"  // Noncompliant
}
