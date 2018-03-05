// S1656
package samples

type User struct { name string }

func (user *User) rename(name string) {
  name = name           // Noncompliant {{Remove or correct this useless self-assignment.}}
  user.name = name      // Compliant
  user.name = user.name // Noncompliant
}
