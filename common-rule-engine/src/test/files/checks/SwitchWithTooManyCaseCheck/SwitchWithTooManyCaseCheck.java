class A{
  void foo() {
    switch (1) { // Noncompliant {{Reduce the number of switch cases from 38 to at most 30.}}
//  ^^^^^^
      case 1:
        System.out.println("");
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      System.out.println("");
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
        break;
      case 1:
      case 1:
      case 1:
      case 1:
        break;
      System.out.println("");
      default:
        System.out.println("");
    }

    switch (1) {
      case 1:
      case 1:
      case 1:
        System.out.println("");
      case 1:
      case 1:
      case 1:
        System.out.println("");
    }
  }
}
