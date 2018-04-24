package StringLiteralDuplicatedCheck

func duplication(s string) {
	duplication("This literal is duplicated")  // Noncompliant {{Define a constant instead of duplicating this literal 4 times.}}
	duplication("This literal is duplicated")
	//				^^^^^^^^^^^^^^^^^^^^^^^^^^^ <
	duplication("This literal is duplicated")
	//				^^^^^^^^^^^^^^^^^^^^^^^^^^^ <
	duplication("This literal is duplicated")
	//				^^^^^^^^^^^^^^^^^^^^^^^^^^^ <
}
