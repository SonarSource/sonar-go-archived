package FixMeTagPresenceCheck

  //Noncompliant@+1
  //fixME
//^^^^^^^

func foo(){
	// foo

	//Noncompliant@+1
    // fixME:
//  ^^^^^^^^^
	// Noncompliant@+1
	/*
	 * fixme
	 * FixMe
	 */

	// Noncompliant@+1
	// FIXME

	// Noncompliant@+1
	// [FIXME]

	// PreFixMe
	// FixMePost
	// PreFixMePost
}
