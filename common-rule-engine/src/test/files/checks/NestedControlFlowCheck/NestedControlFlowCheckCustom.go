package NestedControlFlowCheck

func nesting(condition1, condition2, condition4, condition5 bool) {
	if condition1 {
		/* ... */
		if condition2 {
			/* ... */
			for i := 1; i <= 10; i++ {
				/* ... */
				if condition4 { // Noncompliant {{Refactor this code to not nest more than 3 control flow statements.}}
					if condition5 {
						/* ... */
						if condition4 {

						}
					}
					return
				}
			}
		}
	}
}
