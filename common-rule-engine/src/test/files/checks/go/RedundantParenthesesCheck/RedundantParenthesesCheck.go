package main

func foo() int {
    return ((1 + 1)); // Noncompliant {{Remove these useless parentheses.}}
    //      ^     ^<
}

func bar() {
	return (1 + 1);
}
