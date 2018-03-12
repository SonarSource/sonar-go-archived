import java.time.DayOfWeek;

class Foo {
  void foo(MyEnum myEnum, DayOfWeek dow) {
    switch (0) { // Noncompliant [[sc=5;ec=11]] {{Add a default case to this switch.}}
    }

    switch (0) { // Noncompliant
      case 0:
    }

    switch (0) { // Compliant
      default:
    }

    switch (0) {
      default:   // Compliant --> common-language authorize first position
      case 0:
    }

    switch (myEnum) { // Noncompliant {{Complete cases by adding the missing enum constants or add a default case to this switch.}}
      case A:
        break;
      case B:
        break;
    }

    switch (myEnum) { // Noncompliant ----> java:Compliant: Common rule engine not able to say that all cases of the enum are covered
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

    switch (dow) { // Noncompliant {{Complete cases by adding the missing enum constants or add a default case to this switch.}}
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
      default:  // Noncompliant
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
