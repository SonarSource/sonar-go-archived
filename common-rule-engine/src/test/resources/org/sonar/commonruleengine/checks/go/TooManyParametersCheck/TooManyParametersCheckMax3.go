package main

func nonCompliantFun1(p1, p2, p3, p4 int) {  // Noncompliant
}

func nonCompliantFun2(p1, p2 int, p3, p4 string) {  // Noncompliant
}

func compliantFun1(p1, p2, p3 int) {
}

func compliantFun2(p1, p2 int, p3 string) {
}
