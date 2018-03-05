// S107
package samples

func functionWith8(p1 int, p2 int, p3 int, p4 int, p5 int, p6 int, p7 int, p8 int) { // Noncompliant {{Function has 8 parameters, which is greater than 7 authorized.}}
}

func functionWith7(p1 int, p2 int, p3 int, p4 int, p5 int, p6 int, p7 int) {
}
