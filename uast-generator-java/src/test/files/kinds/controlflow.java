class A {

  void trycatchfinally() {
    try {

    } catch (Exception e) {
      throw e;
    } finally {

    }
  }

  void control_flow(int x, List list) {
    if (true) {

    } else {

    }

    switch (x) {
      case 1:
      case 2:
        break;
      default:

    }

    outer:
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {

      }
      continue outer;
    }

    for (Object o : list) {

    }

    return;

    while (true) {

    }

    do {

    } while (true);
  }

  void ternary() {
    int x = true ? 1 : 2;
  }
}
