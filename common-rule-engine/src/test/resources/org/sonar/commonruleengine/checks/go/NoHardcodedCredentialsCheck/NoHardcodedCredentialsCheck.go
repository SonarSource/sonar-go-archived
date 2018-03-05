package NoHardcodedCredentialsCheck

func test()  {
	pwd := "supersecret" // Noncompliant
	myPassword := "supersecret" // Noncompliant
	url := "login=user&passwd=secret" // Noncompliant
}

func explicitDecl()  {
	var pwd string
	pwd = "secret"
}
