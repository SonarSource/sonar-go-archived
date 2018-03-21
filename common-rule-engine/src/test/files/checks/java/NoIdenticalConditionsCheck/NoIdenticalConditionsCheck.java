class DuplicateConditionIfElseIf {
  void example() {
    if (condition1) {
    } else if (condition1) { // Noncompliant
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
