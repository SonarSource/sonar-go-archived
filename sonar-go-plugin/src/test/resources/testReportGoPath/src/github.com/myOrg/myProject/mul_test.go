package something

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func multiply(x int, y int) int {
	return x * y
}

func TestMultiply(t *testing.T) {
	tests := map[string]struct {
		x        int
		y        int
		expected int
	}{
		"x less than y":           {x: 1, y: 2, expected: 2},
		"y less than x":           {x: 2, y: 1, expected: 2},
		"negative multiplication": {x: 2, y: -1, expected: -2},
	}
	for name, test := range tests {
		t.Run(name, func(t *testing.T) {
			result := multiply(test.x, test.y)
			assert.Equal(t, test.expected, result)
		})
	}
}
