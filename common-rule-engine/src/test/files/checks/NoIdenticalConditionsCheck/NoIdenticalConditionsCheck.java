class DuplicateConditionIfElseIf {
  void example() {
    if (condition1) {
    //  ^^^^^^^^^^>
    } else if (condition1) { // Noncompliant {{This condition is same as one already tested on line 3.}}
    //         ^^^^^^^^^^
    }

    if (condition2) {
    } else if (condition1) {
    } else if (condition1) { // Noncompliant
    }

    if (condition1) {
    } else if (condition2) {
    } else if (condition1) { // Noncompliant 
    }
  }
}
