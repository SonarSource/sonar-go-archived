package main

import "fmt"

func main() {
	sum := 0
	i := 0

	for i := 0; i < 10; i++ {
		sum += i
	}

  // excluded because it is often used with empty body block for iterating until some condition is met
  for i = 0; someCondition(); i++ {
  }

  // "foreach" with empty body is used in golang to empty the channel
  for range someChannel {
  }

  if (i < 0) {
    // with comment inside
  }

  // Noncompliant@+1
	if x < 0 {
	}

	if x < 0 {
	  y := 1
  // Noncompliant@+1
	} else {
	}

  // Noncompliant@+1
	switch 42 {
	}

	switch 42 {
	  case 1:
	  case 2:
	    y := 1
	}

	fmt.Println(sum)

  // excluded because it is often used as "sleep"
	select {}
}

// ok, another rule RSPEC-1186
func empty() {
}
