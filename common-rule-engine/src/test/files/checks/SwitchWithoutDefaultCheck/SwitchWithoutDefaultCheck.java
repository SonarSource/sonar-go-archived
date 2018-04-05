import java.time.DayOfWeek;

class Foo {
  void foo(MyEnum myEnum, DayOfWeek dow) {
    switch (0) { // Noncompliant {{Add a default case to this switch.}}
    }

    switch (0) { // Noncompliant
      case 0:
    }

    switch (0) { // Compliant
      default:
    }

    switch (0) {
      default:   // Compliant -->  order is handled by S4524
      case 0:
    }

    switch (myEnum) { // Noncompliant
      case A:
        break;
      case B:
        break;
    }

    // java:Compliant: Common rule engine not able to say that all cases of the enum are covered
    switch (myEnum) { // Noncompliant
      case A:
      case B:
      case C:
        break;
    }

    switch (myEnum) { // Compliant
      case A:
      case B:
        break;
      default:
        break;
    }

    switch (dow) { // Noncompliant
      case FRIDAY:
        break;
      case MONDAY:
        break;
    }

    switch (myEnum) { // Noncompliant
      case A:
      case B:
        switch (dow) { // Compliant
          case FRIDAY:
            break;
          default:
            break;
        }
        break;
    }

    switch (myEnum) {
      case A:
        break;
      default:  // Compliant - order is handled by S4524
        break;
      case B:
        break;
    }

    switch (myEnum) { // Compliant
      case A:
        switch (dow) { // Compliant
          case FRIDAY:
            break;
          default:
            break;
        }
        break;
      case B:
        break;
      default:
    }
  }
}

enum MyEnum {
  A, B, C;

  MyEnum field;
}
