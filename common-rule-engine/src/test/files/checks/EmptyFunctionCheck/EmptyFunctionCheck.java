class A{
  // Noncompliant@+1
  public A() {

  }

  // Compliant
  public String bar(int a) {
    return "";
  }

// Noncompliant@+1 {{Add a nested comment explaining why this function is empty or complete the implementation.}}
  private foo(){
  }

  // Noncompliant@+1
  public alpha(String a){
  }

  private beta(int a); // Compliant
}

enum AEnum {
  ;
}

class ANestedEnum {
  enum B {
    ;

    // Noncompliant@+1
    public void f() {
    }
  }
}
