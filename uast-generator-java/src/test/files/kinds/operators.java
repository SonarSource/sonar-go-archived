class A {
  int x;
  boolean b;

  void binary() {
    x = 1 + 2;
    x = 1 - 2;
    x = 1 * 2;
    x = 1 / 2;
    x = 1 % 2;
    x = 1 << 2;
    x = 1 >> 2;
    x = 1 & 2;
    x = 1 | 2;
    x = 1 ^ 2;

    b = 1 == 2;
    b = 1 != 2;
    b = 1 < 2;
    b = 1 <= 2;
    b = 1 >= 2;
    b = 1 > 2;
    b = b && true;
    b = b || false;
  }

  void unary() {
    x = +1;
    x = -1;
    b = !b;
  }

  void incdec() {
    x++;
    ++x;
    x--;
    --x;
  }
}
