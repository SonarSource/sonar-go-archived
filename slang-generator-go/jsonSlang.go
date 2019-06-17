package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"strconv"
	"strings"
)

func toJsonSlang(node *Node, comments []*Node, tokens []*Token) string {
	var buf bytes.Buffer
	indent := "  "
	buf.WriteString("{ \n")
	marshallSlangMetaData(&buf, comments, tokens, indent)
	marshalIndentSlang(&buf, node, "  ", indent)
	buf.WriteString("\n} \n")
	return string(buf.Bytes())
}

func marshallSlangMetaData(dst *bytes.Buffer, comments []*Node, tokens []*Token, indent string) {
	dst.WriteString(indent + "\"treeMetaData\": {\n")
	dst.WriteString(strings.Repeat(indent, 2) + "\"comments\": [\n")
	for _, element := range comments {
		dst.WriteString(strings.Repeat(indent, 3))
		marshallComment(dst, element, strings.Repeat(indent, 3))
	}
	dst.WriteString(strings.Repeat(indent, 2) + "],\n")

	dst.WriteString(strings.Repeat(indent, 2) + "\"tokens\": [\n")
	for _, element := range tokens {
		dst.WriteString(strings.Repeat(indent, 3))
		writeObjectSlang(dst,element)
		dst.WriteString(",\n")
	}
	dst.WriteString("\n" + strings.Repeat(indent, 2) + "]\n")

	dst.WriteString(indent + "},")
}

func marshallComment(dst *bytes.Buffer, comment *Node, prefix string) {
	text := comment.Token.Value
	var contentText string

	textRange := comment.TextRange
	textContentRange := TextRange(*textRange)
	textContentRange.StartColumn = textContentRange.StartColumn + 2

	if 	strings.HasPrefix(text, "//") {
		contentText = text[2:]
	} else if strings.HasPrefix(text,"/*") {
		contentText = text[2:len(text)-2]
		textContentRange.EndColumn = textContentRange.EndColumn - 2
	} else {
		panic("Unknown comment content: " + text)
	}

	dst.WriteString(prefix + "{\"text\":\"" + text + "\", \"contentText\":\"" + contentText + "\", \"range\":")
	writeObjectSlang(dst, textRange)
	dst.WriteString(", \"contentRange\": ")
	writeObjectSlang(dst, textContentRange)
	dst.WriteString("}\n")
}

func marshalIndentSlang(dst *bytes.Buffer, node *Node, prefix, indent string) {
	if node == nil {
		return
	}

	dst.WriteByte('{')
	dst.WriteString("\"@type\": ")
	writeObjectSlang(dst, node.SlangType)

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

}

func writeObjectSlang(dst *bytes.Buffer, obj interface{}) {
	b, err := json.Marshal(obj)
	if err != nil {
		panic(err)
	}
	dst.Write(b)
}

func (t TextRange) MarshalJSON() ([]byte, error) {
	return []byte(fmt.Sprintf("\"%d:%d:%d:%d\"", t.StartLine,t.StartColumn,t.EndLine,t.EndColumn)), nil
}