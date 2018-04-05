class A {

  void switchStatement() {
    switch (1) { // Noncompliant {{Remove this conditional structure or edit its code blocks so that they're not all the same.}}
      case 1:
        break;
      case 2:
        break;
      default:
        break;
    }

    switch (1) { // Compliant
      case 1:
        break;
      case 2:
        break;
      case 3:
        f();
        break;
    }
  }

  void ifStatement() {
    if (b == 0) {  // Noncompliant
      doOneMoreThing();
    }
    else {
      doOneMoreThing();
    }

    if (true) { // Noncompliant

    } else if (true) {

    } else {

    }

    if (true) f(); // Noncompliant
    else f();


  }


}
