package main

func foo() int {
  switch a {
    case 0, // Noncompliant {{CONDITION}}
//       ^
         1: return 1 // Noncompliant {{CONDITION}}
//       ^
    case 2: // Noncompliant {{CONDITION}}
//       ^
    fallthrough // Noncompliant {{FALLTHROUGH}}
//  ^^^^^^^^^^^
    case 3: return 3 // Noncompliant {{CONDITION}}
//       ^
    default: return 4
  }

  switch {
    case a == 0, // Noncompliant {{CONDITION}}
//       ^^^^^^
         a == 1: return 1 // Noncompliant {{CONDITION}}
//       ^^^^^^
    case a == 2: return 2 // Noncompliant {{CONDITION}}
//       ^^^^^^
    default: return 3
  }

  switch v := i.(type) {
    case int: // Noncompliant {{CONDITION}}
//       ^^^
      return 1
    case string: // Noncompliant {{CONDITION}}
//       ^^^^^^
      return 2
    default:
      return 3
  }
}
