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

func Test_I_m_diabolic(t *testing.T) {
  t.Fatal("I'm diabolic")
}

func Test_I_m_shy(t *testing.T) {
  t.Skip("I'm shy")
}

func Test_I_m_scared(t *testing.T) {
  panic("I'am scared")
}
