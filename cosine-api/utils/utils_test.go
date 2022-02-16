package utils

import (
	"testing"
	"time"
)

func TestCosine(t *testing.T) {
	t.Log("Starting unit test at " + time.Now().String())
	var b1, b2 []byte
	b1 = []byte("I love LA and New York")
	b2 = []byte("I love New York and CIaaione3fdsf")
	res := Cosine(b1, b2)
	t.Logf("Result of Cosine is %f", res)
	return
}
