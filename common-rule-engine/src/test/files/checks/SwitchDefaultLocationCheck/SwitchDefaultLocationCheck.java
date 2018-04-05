import java.time.DayOfWeek;

class Foo {
  void foo(MyEnum myEnum, DayOfWeek dow) {
    switch (0) { // Compliant
    }

    switch (0) { // Compliant
      case 0:
    }

    switch (0) { // Compliant
      default:
    }

    switch (0) {
      default:   // Compliant
      case 0:
    }

    switch (myEnum) { // Compliant
      case A:
        break;
      case B:
        break;
    }

    switch (myEnum) { // Compliant
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

    switch (dow) { // Compliant
      case FRIDAY:
        break;
      case MONDAY:
        break;
    }

    switch (myEnum) { // Compliant
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
      default:  // Noncompliant {{Move this "default" case clause to the beginning or end of this "switch" statement.}}
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
