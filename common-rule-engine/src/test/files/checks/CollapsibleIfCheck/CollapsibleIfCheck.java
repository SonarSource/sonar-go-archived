class A {
  void f() {
    if (false) {
    }

    if (false) {
    } else {
    }

    if (false) {  // Noncompliant {{Merge this if statement with the nested one.}}
 // ^^
      if (false) {
  //  ^^ <
      }
    }

    if (false) {
      if (false) {
      }
      System.out.println();
    }

    if (false) {
      int a;
      if (a) {
      }
    }

    if (false) {
      if (false) {
      }
    } else {
    }

    if (false) {
      if (false) {
      } else {
      }
    }

    if (false) {
    } else if (false) { // Noncompliant {{Merge this if statement with the nested one.}}
      if (false) {
      }
    }

    if (false)      // Noncompliant {{Merge this if statement with the nested one.}}
      if (true) {
      }

    if (false) {
      while (true) {
        if (true) {
        }
      }

      while (true)
        if(true) {
        }
    }
  }

  {
    if (false) {
    }
    if (false) {
      switch ("SELECT") {
        case "SELECT":
          if ("SELECT".equals(token.getValue())) {
          }
          break;
      }
    }

    if (true) {  // Noncompliant
      if (true) {
        int a;
        if (true) {
          int b;
        }
      }
    }

    if (false) {
      while (true) {
        if (true) {
        }
      }
    }

    if (true)  // Noncompliant
      if (false)  // Noncompliant
        if (true)
          a = 0;
  }
}
