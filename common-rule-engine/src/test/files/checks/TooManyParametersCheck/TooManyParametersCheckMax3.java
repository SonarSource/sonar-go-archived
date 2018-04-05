class TooManyParameters {
  TooManyParameters(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8) { // Noncompliant
  }

  void method(int p1, int p2, int p3, int p4) { // Noncompliant
  }

  void otherMethod(int p1, int p2, int p3) {}
}
