// foo
// Noncompliant@+1
// Noncompliant@+1 {{Take the required action to fix the issue indicated by this "FIXME" comment.}}
// fixME
class A {
  // Noncompliant@+1
  /*
   * fixme
   * FixMe
   */
  //

  // Noncompliant@+1
  // FIXME
//^^^^^^^^

  // Noncompliant@+1
  // [FIXME]

  // PreFixMe
  // FixMePost
  // PreFixMePost
}
