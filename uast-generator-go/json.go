package main

import (
	"bytes"
	"encoding/json"
)

func toJson(node *Node) string {
	var buf bytes.Buffer
	marshalIndent(&buf, node, "  ", "  ")
	return string(buf.Bytes())
}

func marshalIndent(dst *bytes.Buffer, node *Node, prefix, indent string) {
	if node == nil {
		return
	}

	dst.WriteByte('{')

	if len(node.Kinds) > 0 {
		dst.WriteString("\"kinds\": ")
		writeObject(dst, node.Kinds)
		dst.WriteString(", ")
	}

	if node.Token != nil {
		dst.WriteString("\"token\": ")
		writeObject(dst, node.Token)
		dst.WriteString(", ")
	}

	dst.WriteString("\"nativeNode\": \"" + node.NativeNode + "\"")

	if len(node.Children) > 0 {
		dst.WriteString(", \"children\": [\n" + prefix)
		size := len(node.Children)
		for i := 0; i < size-1; i++ {
			child := node.Children[i]
			marshalIndent(dst, child, prefix+indent, indent)
			dst.WriteString(",\n" + prefix)
		}
		marshalIndent(dst, node.Children[size-1], prefix+indent, indent)
		dst.WriteString("\n" + prefix[0:len(prefix)-len(indent)] + "]")
	}
	dst.WriteString("}")
}

func writeObject(dst *bytes.Buffer, obj interface{}) {
	b, err := json.Marshal(obj)
	if err != nil {
		panic(err)
	}
	dst.Write(b)
}
