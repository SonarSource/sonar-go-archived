package main

import (
	"bytes"
	"encoding/json"
	"strconv"
)

func toJsonSlang(node *Node) string {
	var buf bytes.Buffer
	marshalIndentSlang(&buf, node, "  ", "  ")
	return string(buf.Bytes())
}

func marshalIndentSlang(dst *bytes.Buffer, node *Node, prefix, indent string) {
	if node == nil {
		return
	}

	dst.WriteByte('{')
		dst.WriteString("\"SlangTree\": ")
		writeObjectSlang(dst, node.SlangTree)
		
		if node.TextRange != nil {
			dst.WriteString(",\"TextRange\": ")
			writeObjectSlang(dst, node.TextRange)
		}

			dst.WriteString(",\"offset\": " + strconv.FormatInt(int64(node.offset), 10))
			dst.WriteString(", ")
		
			dst.WriteString(",\"endOffset\": " + strconv.FormatInt(int64(node.endOffset), 10))
			dst.WriteString(", ")
		
		if node.Token != nil {
			dst.WriteString(",\"token\": ")
			writeObjectSlang(dst, node.Token)
			dst.WriteString(", ")
		}
		
		if len(node.Children) > 0 {
			dst.WriteString(", \"children\": [{\n" + prefix)
			size := len(node.Children)
			for i := 0; i < size-1; i++ {
				child := node.Children[i]
				dst.WriteString(child.ParentField + ": ")
				marshalIndentSlang(dst, child, prefix+indent, indent)
				dst.WriteString(",\n" + prefix)
			}
			lastChildren := node.Children[size-1]
			dst.WriteString(lastChildren.ParentField + ": ")
			marshalIndentSlang(dst, lastChildren, prefix+indent, indent)
			dst.WriteString("\n" + prefix[0:len(prefix)-len(indent)] + "}]")
		}
	dst.WriteString("}")

}

func writeObjectSlang(dst *bytes.Buffer, obj interface{}) {
	b, err := json.Marshal(obj)
	if err != nil {
		panic(err)
	}
	dst.Write(b)
}