package pkg1

import "testing"

func Test_func1(t *testing.T) {
	expected := 2
	actual := func1(1, 2)
	if expected != actual {
		t.Fatalf("got %v; expected %v", actual, expected)
	}
}
