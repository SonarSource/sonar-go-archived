class A {
  int a = 0;
  int b = 1;
  int c = 42;
  int mode = 0644; // 4-digit octal numbers are ignored as are often used for file permissions
  int d = 010; // Noncompliant {{Use decimal rather than octal values.}}
//        ^^^
  int e = 00; // Noncompliant
  int f = 0.;
  int g = 0x00;
  int h = 0X00;
  int j = 0b0101;
  int k = 0B0101;

  void foo() {
    int x = 010; // Noncompliant
  }
}
