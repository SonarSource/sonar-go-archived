package main
func foo() {
  if (false) {
  }

  if (false) {
  } else {
  }

  if (false) {  // Noncompliant {{Merge this if statement with the nested one.}}
//^^
    if (false) {
//  ^^ <
    }
  }

  if (false) {
    if (false) {
    }
    foo()
  }

  if (false) {
    var a = 3
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

  if (false) {
    for (true) {
      if (true) {
      }
    }

    for (true) {
      if(true) {
      }
    }
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
      var a = 1
      if (true) {
        var b = 2
      }
    }
  }

  if (false) {
    for (true) {
      if (true) {
      }
    }
  }

}
