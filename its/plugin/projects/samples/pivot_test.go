package samples

import (
	"testing"
	"strings"
	"io"
	"bytes"
)

func Test_rot13(t *testing.T) {
	s := strings.NewReader("Lbh penpxrq gur pbqr!")
	r := rot13Reader{s}
	var buf bytes.Buffer
	io.Copy(&buf, &r)

	expected := "You cracked the code!"
	if actual := buf.String(); actual != expected {
		t.Fatalf("got '%v'; expected %v", actual, expected)
	}
}
