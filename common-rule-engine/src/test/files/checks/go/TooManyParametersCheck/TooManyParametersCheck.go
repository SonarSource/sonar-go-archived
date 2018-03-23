package main
// FIXME(issue #152) should only highlight function signature
  func nonCompliantFun1(p1, p2, p3, p4, p5, p6, p7, p8 int) {  // Noncompliant
//^[el=+2;ec=1]
}

func nonCompliantFun2(p1, p2, p3, p4 int, p5, p6, p7, p8 string) {  // Noncompliant
}

func compliantFun1(p1, p2, p3, p4, p5, p6, p7 int) {
}

func compliantFun2(p1, p2, p3, p4 int, p5, p6, p7 string) {
}
