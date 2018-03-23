class A {
  void foo() {
    boolean a,b;
    if(a == b) { }
    if(a == a) { } // Noncompliant {{Correct one of the identical sub-expressions on both sides of operator "==".}}
    // ^>   ^
    if(a != a) { } // Noncompliant
    if(a || a) { } // Noncompliant
    if(a && a) { } // Noncompliant
    if(a == b || a == b) {} // Noncompliant
    if(a || b || a) {} // XXXcompliant ---> SonarJava: handle operator symmetry
    if(a || a || b) {} // Noncompliant
    if(a || b || c || e && a) {}
    if(a && b && c && e && a) {} // XXXcompliant ---> SonarJava: handle operator symmetry
    if(b
        || a
        || a) {} // XXXcompliant ---> SonarJava: handle operator symmetry

    double d = 0.0d;
    float f = 0.0f;
    // SonarJava: valid test for NaN
    if(f != f) {} // Noncompliant
    if(d != d) {} // Noncompliant
    int j,l;
    // SonarJava:exclude this case for bit masks
    int k = 1 << 1; // Noncompliant
    j = 12 - k -k; //case why minus is excluded.
    j = k - k; // Noncompliant
    j = k*3/12%2 - k*3/12%2; // Noncompliant

    int v1 = 1024 * 1024; // Compliant
    int v2 = 1 + 1; // Compliant
  }

  void fun(Object a, Object b) {
    a.equals(a);  // XXXcompliant
    a.equals(b);
    equals(a);
    java.util.Objects.equals(a, a); // XXXcompliant
    java.util.Objects.equals(a, b);
    java.util.Objects.deepEquals(a, a); // XXXcompliant
    java.util.Objects.deepEquals(a, b);
    java.util.Objects.isNull(a);
  }
}
