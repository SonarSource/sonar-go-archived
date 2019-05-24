package NoHardcodedIpAddressCheck


const defaultHost = "http://127.0.0.1:8080"


func foo() string{
	var ip = "0.0.0.0"

	ip = "http://0.1.2.3:"
	ip = ""
	ip = "http://192.168.0.1/admin.html" // Noncompliant {{Make this IP "192.168.0.1" address configurable.}}
	  // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	ip = "http://www.example.org"
	ip = "0.0.0.1234"
	ip = "0.0.0.0:1234/admin.html"

	ip = "1234.0.0.0"
	ip = "1234.0.0.0.0.1234"
	ip = "1.1.1.1:809765"
	ip = ".0.0.0.0"
	ip = "0.256.0.0"
	ip = "1."
	ip = ""
	ip = "v0.0.1.200__do_something.sql" // Compliant - suffixed and prefixed
	ip = "1.0.0.0-1" // Compliant - suffixed
	ip = "127.1.2.3" // Compliant - loopback
	ip = "255.255.255.255" // Compliant - broadcast
	return ip
}
