package main

func counting_loop() {
	for i := 0; i < 10; i++ {
		foo()
	}
}

func counting_loop_break() {
	for i := 0; i < 10; i++ {
		   foo()
		   break // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^
	}
}

func counting_loop_break_conditional(x int) {
	for i := 0; i < 10; i++ {
		foo()
		if i == x {
			break
		}
	}
}

func counting_loop_return() {
	for i := 0; i < 10; i++ {
		   foo()
		   return // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^
	}
}

func counting_loop_return_conditional(x int) {
	for i := 0; i < 10; i++ {
		foo()
		if i == x {
			return
		}
	}
}

func counting_loop_continue() {
	for i := 0; i < 10; i++ {
		   foo()
		   continue // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^^^
	}
}

func counting_loop_continue_conditional(x int) {
	for i := 0; i < 10; i++ {
		foo()
		if i == x {
			continue
		}
		break
	}
}

func counting_loop_continue_conditional_nested(x int) {
	for i := 0; i < 10; i++ {
		foo()
		if 0 < x {
			if i == x {
				continue
			}
		}
		break
	}
}

func counting_loop_panic() {
	for i := 0; i < 10; i++ {
		   foo()
		   panic("BOOM!") // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^^^^^^^^^
	}
}

func counting_loop_panic_conditional(x int) {
	for i := 0; i < 10; i++ {
		foo()
		if i == x {
			panic("BOOM!")
		}
	}
}

func while_loop() {
	i := 0
	for i < 10 {
		i++
		foo()
	}
}

func while_loop_break() {
	i := 0
	for i < 10 {
		   i++
		   foo()
		   break // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^
	}
}

func while_loop_break_conditional(x int) {
	i := 0
	for i < 10 {
		i++
		foo()
		if i == x {
			break
		}
	}
}

func while_loop_return() {
	i := 0
	for i < 10 {
		   i++
		   foo()
		   return // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^
	}
}

func while_loop_return_conditional(x int) {
	i := 0
	for i < 10 {
		i++
		foo()
		if i == x {
			return
		}
	}
}

func while_loop_continue() {
	i := 0
	for i < 10 {
		   i++
		   foo()
		   continue // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^^^
	}
}

func while_loop_continue_conditional(x int) {
	i := 0
	for i < 10 {
		i++
		foo()
		if i == x {
			continue
		}
		break
	}
}

func while_loop_continue_conditional_nested(x int) {
	i := 0
	for i < 10 {
		i++
		foo()
		if 0 < x {
			if i == x {
				continue
			}
		}
		break
	}
}

func while_loop_panic() {
	i := 0
	for i < 10 {
		   i++
		   foo()
		   panic("BOOM!") // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^^^^^^^^^
	}
}

func while_loop_panic_conditional(x int) {
	i := 0
	for i < 10 {
		i++
		foo()
		if i == x {
			panic("BOOM!")
		}
	}
}

func range_loop(s []interface{}) {
	for _, item := range s {
		bar(item)
	}
}

func range_loop_break(s []interface{}) {
	for _, item := range s {
		bar(item)
		break
	}
}

func range_loop_break_conditional(s []interface{}, x int) {
	for _, item := range s {
		bar(item)
		if 1 == x {
			break
		}
	}
}

func range_loop_break_nested(s []interface{}, x int) {
	for _, item1 := range s {
		bar(item1)
		for _, item2 := range s {
			bar(item2)
			if 1 == x {
				continue
			}
		}
		break
	}
}

func range_loop_return(s []interface{}) {
	for _, item := range s {
		bar(item)
		return
	}
}

func range_loop_return_conditional(s []interface{}, x int) {
	for _, item := range s {
		bar(item)
		if 1 == x {
			return
		}
	}
}

func range_loop_continue(s []interface{}) {
	for _, item := range s {
		bar(item)
		continue
	}
}

func range_loop_continue_conditional(s []interface{}, x int) {
	for _, item := range s {
		bar(item)
		if 1 == x {
			continue
		}
		break
	}
}

func range_loop_continue_conditional_nested(s []interface{}, x int) {
	for _, item := range s {
		bar(item)
		if 0 < x {
			if 1 == x {
				continue
			}
		}
		break
	}
}

func range_loop_panic(s []interface{}) {
	for _, item := range s {
		bar(item)
		panic("BOOM!")
	}
}

func range_loop_panic_conditional(s []interface{}, x int) {
	for _, item := range s {
		bar(item)
		if 1 == x {
			panic("BOOM!")
		}
	}
}

func range_loop_return_first(m map[int]int) int {
	for _, item := range m {
		return item
	}
	return 0
}

func infinite_loop() {
	for {
		foo()
	}
}

func infinite_loop_break() {
	for {
		   foo()
		   break // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^
	}
}

func infinite_loop_break_conditional(x int) {
	for {
		foo()
		if 1 == x {
			break
		}
	}
}

func infinite_loop_return() {
	for {
		   foo()
		   return // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^
	}
}

func infinite_loop_return_conditional(x int) {
	for {
		foo()
		if 1 == x {
			return
		}
	}
}

func infinite_loop_continue() {
	for {
		   foo()
		   continue // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^^^
	}
}

func infinite_loop_continue_conditional(x int) {
	for {
		foo()
		if 1 == x {
			continue
		}
		break
	}
}

func infinite_loop_continue_conditional_nested(x int) {
	for {
		foo()
		if 0 < x {
			if 1 == x {
				continue
			}
		}
		break
	}
}

func infinite_loop_panic() {
	for {
		   foo()
		   panic("BOOM!") // Noncompliant {{Remove this unconditional jump or make it conditional.}}
		// ^^^^^^^^^^^^^^
	}
}

func infinite_loop_panic_conditional(x int) {
	for {
		foo()
		if 1 == x {
			panic("BOOM!")
		}
	}
}

func foo() {}

func bar(v interface{}) {}
