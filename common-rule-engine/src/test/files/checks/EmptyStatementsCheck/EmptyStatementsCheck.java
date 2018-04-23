/* Go lang demands curly braces and semicolon is not required to end a statement,
 * therefore we report an issue each time we find an empty statement*/

class A{

  public Foo() {
    ; // Noncompliant {{Remove this empty statement.}}
  }

  void foo(boolean condition) {

    for (; i < 10; i++ ){
      ; // Noncompliant {{Remove this empty statement.}}
    }

    for( ; i < 10; i++ ){
      ; // Noncompliant {{Remove this empty statement.}}
      i+=2;
      break;
    }

    if (i == 0)
      ; // Noncompliant {{Remove this empty statement.}}
    else
      ; // Noncompliant {{Remove this empty statement.}}

    if (a == 0)
      ; // Noncompliant {{Remove this empty statement.}}

    do ; while (condition); // Noncompliant

    while (condition)
      ; // Noncompliant

    for (Object object : getCollection())
      ;                   // Noncompliant

    return; // Compliant
  }

}
