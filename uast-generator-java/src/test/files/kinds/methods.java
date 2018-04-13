class A {

  A(int x) {

  }

  void foo() {
    call(1, 2 * 2);
    noarg();
    A a = new A(5);
    a.call(1, 2);
  }

  void call(int x, int y) {

  }

  void noarg() {

  }
}
