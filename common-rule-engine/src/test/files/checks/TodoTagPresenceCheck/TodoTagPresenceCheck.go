package main

// foo

  // Noncompliant@+1
  // toDO
//^^^^^^^
// Noncompliant@+1
/*

  todo
  ToDo
*/
  // Noncompliant@+1
  // Noncompliant@+1 {{Take the required action to fix the issue indicated by this "TODO" comment.}}
  // TODO
//^^^^^^^
// Noncompliant@+1
// TODO Explanation

// Noncompliant@+1
// [TODO]

// Noncompliant@+1
// TODO: Inefficient conversion - can we improve it?

// PreTodo
// toDomain
