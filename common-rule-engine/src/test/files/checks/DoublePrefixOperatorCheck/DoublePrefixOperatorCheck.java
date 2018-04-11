class test1 {
  int a = 1;
  boolean flag = true;
  int hash = 1;
  int hashCode = 2;

  boolean flag2 = !!flag;  // Noncompliant
  int a1 = ~~~a; // Noncompliant {{Use the "~" operator just once or not at all.}}

  boolean flag3 = !!!flag; // Noncompliant

  boolean flag4 = !!!foo(); // Noncompliant 

  boolean flag5 = !(!flag4); // Noncompliant  {{Use the "!" operator just once or not at all.}}

  int c = ~(~(~a3));    // Noncompliant  {{Use the "~" operator just once or not at all.}}

  boolean flag6 = !(!(!flag4)); // Noncompliant {{Use the "!" operator just once or not at all.}}

  int a3 =  - - -a2;  // Noncompliant 

  int a4 =  - -a2;  // Noncompliant 

  int a5 =  + + +a2;  // Noncompliant 

  int a6 =  + +a2;  // Noncompliant 

  int a2 = ~~a; // Noncompliant

  int a9 = ~(~a); // Noncompliant

  int a10 = ~(~(a9 - 9));  // Noncompliant

  int a7 = --a2;  // Compliant
  int a8 = ++a2;  // Compliant
  boolean flag1 = !flag4;  // Compliant
  int b = ~a1;  // Compliant
  boolean flag7 = !foo(); // Compliant
  int h1 = ~~hash; // Noncompliant
  int h2 = ~~hashCodep; // Noncompliant
}
