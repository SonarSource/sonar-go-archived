package DoublePrefixOperatorCheck
func test1() {
  var a int = 1;
  var flag bool = true;
  var hash int = 1;
  var hashCode int = 2;

  var a1 int = ^^^a; // Noncompliant {{Use the "^" operator just once or not at all.}}
  var flag2 bool  = !!flag;  // Noncompliant
  var flag3 bool  = !!!flag; // Noncompliant
  var flag4 bool  = !!!foo(); // Noncompliant
  var flag5 bool  = !(!flag4); // Noncompliant  {{Use the "!" operator just once or not at all.}}
  var flag6 bool  = !(!(!flag4)); // Noncompliant {{Use the "!" operator just once or not at all.}}
  var c int = ^(^(^a3));    // Noncompliant  {{Use the "^" operator just once or not at all.}}

  var a3  int =  - - -a2;  // Noncompliant
  var a4  int =  - -a2;  // Noncompliant
  var a5  int =  + + +a2;  // Noncompliant
  var a6  int =  + +a2;  // Noncompliant
  var a2  int = ^^a; // Noncompliant
  var a9  int = ^(^a); // Noncompliant
  var a10 int  = ^(^(a9 - 9));  // Noncompliant

  var flag1 bool = !flag4;  // Compliant
  var b =int   ^a1;  // Compliant
  var flag7 bool = !foo(); // Compliant
  var h1 int  = ^^hash; // Noncompliant
  var h2 int  = ^^hashCodep; // Noncompliant
}
