package NoHardcodedIpAddressCheck


const defaultHost = "http://127.0.0.1:8080"  // Noncompliant

func foo() string{
	var ip = "0.0.0.0" // Noncompliant {{Make this IP "0.0.0.0" address configurable.}}

	ip = ""
	ip = "http://192.168.0.1/admin.html" // Noncompliant
	ip = "http://www.example.org"
	ip = "0.0.0.1234"
	ip = "1234.0.0.0"
	ip = "1234.0.0.0.0.1234"
	ip = ".0.0.0.0"
	ip = "0.256.0.0"
	ip = "1."
	ip = "v0.0.1.200__do_something.sql" // Compliant - suffixed and prefixed
	ip = "1.0.0.0-1" // Compliant - suffixed
	return ip
}
