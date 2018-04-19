class Bad_Method_Name {
  public Bad_Method_Name() {
  }

  void bad_name() { // Noncompliant {{Rename 'bad_name' to match the regular expression ^[a-zA-Z0-9]+$.}}
//     ^^^^^^^^
  }

  void good() {
  }

  // FP
  @Override
  void bad_but_overrides(){ // Noncompliant
  }

  @Deprecated
  void bad_2() { // Noncompliant
  }

}
